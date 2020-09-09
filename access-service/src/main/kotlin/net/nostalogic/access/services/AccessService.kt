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
import net.nostalogic.exceptions.NoDeleteException
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

    open fun getPolicy(policyId: String): Policy {
        return queryService.getPolicy(policyId)
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
        if (StringUtils.isNotBlank(policyEdit.name))
            existingPolicy.name = policyEdit.name
        policyEdit.status?.let { existingPolicy.status = policyEdit.status }
        policyEdit.priority?.let {existingPolicy.priority = policyEdit.priority }
        validatePolicy(existingPolicy, false)

        try {
            matchPolicyApplicationChanges(policyId, existingPolicy, policyEdit)
            return savePolicy(existingPolicy, policyId)
        } catch (e: Exception) {
            throw NoSaveException(205002, "policy", null, e)
        }
    }

    fun deletePolicy(policyId: String) {
        val policies = searchPolicies(PolicySearchCriteria(policyIds = setOf(policyId), status = setOf(*EntityStatus.values())))
        if (policies.isEmpty())
            throw NoDeleteException(203001, "Policy", "Policy not found for deletion: ${policyId}")
        changePolicyStatuses(setOf(policyId), EntityStatus.DELETED)
    }

    @Transactional
    open fun changePolicyStatuses(policyIds: Collection<String>, status: EntityStatus) {
        policyRepository.changePoliciesToStatus(policyIds, status)
    }

    /**
     * If subjects, resources, or permissions in an update to a policy are not null, this removes any persisted records
     * not present in the edit and sets the edited values to the current policy.
     */
    @Transactional
    open fun matchPolicyApplicationChanges(policyId: String, currentPolicy: Policy, editedPolicy: Policy) {
        editedPolicy.subjects?.let { currentPolicy.subjects!!.removeAll(editedPolicy.subjects!!) }
        editedPolicy.resources?.let { currentPolicy.resources!!.removeAll(editedPolicy.resources!!) }
        val actions = currentPolicy.permissions!!.keys
        editedPolicy.permissions?.let { actions.removeAll(editedPolicy.permissions!!.keys) }
        if (currentPolicy.subjects!!.isNotEmpty())
            policySubjectRepository.deleteAllByIdIn(currentPolicy.subjects!!
                    .map { s -> compositePolicyId(policyId, EntityUtils.toEntityRef(s)) }.toSet())
        if (currentPolicy.resources!!.isNotEmpty())
            policyResourceRepository.deleteAllByIdIn(currentPolicy.resources!!
                    .map { r -> compositePolicyId(policyId, EntityUtils.toEntityRef(r)) }.toSet())
        if (actions.isNotEmpty())
            policyActionRepository.deleteAllByIdIn(actions.map { a -> compositeActionId(policyId, a) }.toSet())
        editedPolicy.subjects?.let { currentPolicy.subjects = editedPolicy.subjects!! }
        editedPolicy.resources?.let { currentPolicy.resources = editedPolicy.resources!! }
        editedPolicy.permissions?.let { currentPolicy.permissions = editedPolicy.permissions!! }
    }

    @Transactional
    open fun savePolicy(policy: Policy, policyId: String? = null): Policy {
        val policyEntity: PolicyEntity
        if (policyId != null) {
            policyEntity = policyRepository.getOne(policyId)
            policyEntity.name = policy.name!!
            policyEntity.priority = policy.priority!!
            policyEntity.status = policy.status!!
        } else {
            policyEntity = PolicyEntity(policy.name!!, policy.priority!!, SessionContext.getUserId())
            policy.status?.let { policyEntity.status = policy.status!! }
        }
        policyRepository.save(policyEntity)

        policy.permissions?.let { policy.permissions!!.forEach { (k, v) -> policyActionRepository.save(PolicyActionEntity(policyEntity.id, k, v)) } }
        policy.subjects?.let { policy.subjects!!.forEach {
            val ref = EntityUtils.toEntityRef(it)
            policySubjectRepository.save(PolicySubjectEntity(policyEntity.id, ref.id, ref.entity))
        } }
        policy.resources?.let { policy.resources!!.forEach {
            val ref = EntityUtils.toEntityRef(it)
            policyResourceRepository.save(PolicyResourceEntity(policyEntity.id, ref.id, ref.entity))
        } }
        policy.id = policyEntity.id
        return policy
    }

    fun validatePolicy(policy: Policy, create: Boolean = true) {
        val invalidFields = StringJoiner(",")
        if ((create && StringUtils.isBlank(policy.name)) || (policy.name != null && policy.name!!.length > 50))
            invalidFields.add("name")
        if (create && policy.priority == null)
            invalidFields.add("priority")

        policy.resources?.let {
            for (id in policy.resources!!) {
                if (!EntityUtils.isFullId(id)) {
                    invalidFields.add("resources")
                    break
                }
            }
        }

        policy.subjects?.let {
            for (id in policy.subjects!!) {
                val ref = EntityUtils.toEntityRef(id)
                if (!(ref.isSignature() && ALLOWED_SUBJECTS.contains(ref.entity)) && NoEntity.ALL != ref.entity) {
                    invalidFields.add("subjects")
                    break
                }
            }
        }

        if (invalidFields.length() > 0)
            throw NoValidationException(207002, invalidFields.toString())
    }

    fun verifyAccess(allowGuest: Boolean) {
        val grant = SessionContext.currentSession.get().grant
        if (!allowGuest && grant.type == AuthenticationType.GUEST)
            throw NoAccessException(201008, "You must be logged in to perform this action")
    }

}
