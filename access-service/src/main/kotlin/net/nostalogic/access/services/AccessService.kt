package net.nostalogic.access.services

import net.nostalogic.access.models.PolicySearchCriteria
import net.nostalogic.access.persistence.entities.PolicyActionEntity
import net.nostalogic.access.persistence.entities.PolicyEntity
import net.nostalogic.access.persistence.entities.PolicyResourceEntity
import net.nostalogic.access.persistence.entities.PolicySubjectEntity
import net.nostalogic.access.persistence.repositories.PolicyActionRepository
import net.nostalogic.access.persistence.repositories.PolicyRepository
import net.nostalogic.access.persistence.repositories.PolicyResourceRepository
import net.nostalogic.access.persistence.repositories.PolicySubjectRepository
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoAccessException
import net.nostalogic.exceptions.NoSaveException
import net.nostalogic.exceptions.NoValidationException
import net.nostalogic.security.contexts.SessionContext
import net.nostalogic.utils.EntityUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
open class AccessService(
        private val policyRepository: PolicyRepository,
        private val policySubjectRepository: PolicySubjectRepository,
        private val policyResourceRepository: PolicyResourceRepository,
        private val policyActionRepository: PolicyActionRepository,
        private val queryService: AccessQueryService
) {

    companion object {
        val ALLOWED_SUBJECTS = setOf(NoEntity.USER, NoEntity.GROUP)

        fun compositePolicyId(policyId: String, ref: EntityReference): String {
            return "${policyId}::${ref.toShortId()}"
        }

        fun compositeActionId(policyId: String, action: PolicyAction): String {
            return "${policyId}::${action.name}"
        }
    }

    open fun searchPolicies(criteria: PolicySearchCriteria): ArrayList<Policy> {
        return queryService.searchPolicies(criteria)
    }

    open fun createPolicy(policy: Policy): Policy {
        // TODO: user SessionContext to confirm user can create sessions
        validatePolicy(policy)
        try {
            return savePolicy(policy)
        } catch (e: Exception) {
            throw NoSaveException(205001, "policy", null, e)
        }
    }

    open fun editPolicy(policyEdit: Policy, policyId: String): Policy {
        val existingPolicy = queryService.getPolicy(policyId)
        validatePolicy(policyEdit)
        existingPolicy.name = policyEdit.name
        existingPolicy.status = policyEdit.status
        existingPolicy.priority = policyEdit.priority

        try {
            removeDeletedApplications(policyId, existingPolicy, policyEdit)
            return savePolicy(policyEdit, policyId)
        } catch (e: Exception) {
            throw NoSaveException(205002, "policy", null, e)
        }
    }

    fun deletePolicy(policyId: String) {
        changePolicyStatuses(setOf(policyId), EntityStatus.DELETED)
    }

    @Transactional
    open fun changePolicyStatuses(policyIds: Collection<String>, status: EntityStatus) {
        policyRepository.changePoliciesToStatus(policyIds, status)
    }

    @Transactional
    open fun removeDeletedApplications(policyId: String, currentPolicy: Policy, editedPolicy: Policy) {
        currentPolicy.subjects.removeAll(editedPolicy.subjects)
        currentPolicy.resources.removeAll(editedPolicy.resources)
        val actions = currentPolicy.permissions.keys
        actions.removeAll(editedPolicy.permissions.keys)
        if (currentPolicy.subjects.isNotEmpty())
            policySubjectRepository.deleteAllByIdIn(currentPolicy.subjects
                    .map { s -> compositePolicyId(policyId, EntityUtils.toEntityRef(s)) }.toSet())
        if (currentPolicy.resources.isNotEmpty())
            policyResourceRepository.deleteAllByIdIn(currentPolicy.resources
                    .map { r -> compositePolicyId(policyId, EntityUtils.toEntityRef(r)) }.toSet())
        if (actions.isNotEmpty())
            policyActionRepository.deleteAllByIdIn(actions.map { a -> compositeActionId(policyId, a) }.toSet())
    }

    @Transactional
    open fun savePolicy(policy: Policy, policyId: String? = null): Policy {
        val policyEntity: PolicyEntity
        if (policyId != null) {
            policyEntity = policyRepository.getOne(policyId)
            policyEntity.name = policy.name
            policyEntity.priority = policy.priority
            policyEntity.status = policy.status
        } else
            policyEntity = PolicyEntity(policy.name, policy.priority, SessionContext.getUserId())
        policyRepository.save(policyEntity)

        for (action in policy.permissions)
            policyActionRepository.save(PolicyActionEntity(policyEntity.id, action.key, action.value))
        for (subject in policy.subjects) {
            val ref = EntityUtils.toEntityRef(subject)
            policySubjectRepository.save(PolicySubjectEntity(policyEntity.id, ref.id, ref.entity))
        }
        for (resource in policy.resources) {
            val ref = EntityUtils.toEntityRef(resource)
            policyResourceRepository.save(PolicyResourceEntity(policyEntity.id, ref.id, ref.entity))
        }
        policy.id = policyEntity.id
        return policy
    }

    fun validatePolicy(policy: Policy) {
        val invalidFields = StringJoiner(",")
        if (StringUtils.isBlank(policy.name) || policy.name.length > 50)
            invalidFields.add("name")
        if (policy.permissions.isEmpty())
            invalidFields.add("permissions")
        if (policy.resources.isEmpty())
            invalidFields.add("resources")
        if (policy.subjects.isEmpty())
            invalidFields.add("subjects")

        for (id in policy.resources) {
            if (!EntityUtils.isFullId(id)) {
                invalidFields.add("resources")
                break
            }
        }
        for (id in policy.subjects) {
            val ref = EntityUtils.toEntityRef(id)
            if (!(ref.isSignature() && ALLOWED_SUBJECTS.contains(ref.entity)) && NoEntity.ALL != ref.entity) {
                invalidFields.add("subjects")
                break
            }
        }
        if (invalidFields.length() > 0)
            throw NoValidationException(207002, invalidFields.toString(), "Specified fields are empty or invalid")
    }

    fun verifyAccess(allowGuest: Boolean) {
        val grant = SessionContext.currentSession.get().grant
        if (!allowGuest && grant.type == AuthenticationType.GUEST)
            throw NoAccessException(201008, "You must be logged in to perform this action")
    }

}
