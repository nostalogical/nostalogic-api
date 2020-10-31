package net.nostalogic.access.services

import net.nostalogic.access.datamodel.PolicySearchCriteria
import net.nostalogic.access.mappers.PolicyMapper
import net.nostalogic.access.persistence.repositories.PolicyActionRepository
import net.nostalogic.access.persistence.repositories.PolicyRepository
import net.nostalogic.access.persistence.repositories.PolicyResourceRepository
import net.nostalogic.access.persistence.repositories.PolicySubjectRepository
import net.nostalogic.access.validators.PolicyValidator
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoAccessException
import net.nostalogic.exceptions.NoDeleteException
import net.nostalogic.exceptions.NoSaveException
import net.nostalogic.security.contexts.SessionContext
import net.nostalogic.utils.EntityUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Service
import java.util.*

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
            return "${policyId}::${ref.toLocalReference()}"
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

    open fun getPolicies(criteria: PolicySearchCriteria): ArrayList<Policy> {
        return queryService.filterPolicies(criteria)
    }

    open fun createPolicy(policy: Policy): Policy {
        // TODO: user SessionContext to confirm user can create sessions
        PolicyValidator.validate(policy)
        try {
            return savePolicy(policy)
        } catch (e: Exception) {
            throw NoSaveException(205001, "policy", e)
        }
    }

    open fun editPolicy(policyEdit: Policy, policyId: String): Policy {
        val existingPolicy = queryService.getPolicy(policyId)
        if (StringUtils.isNotBlank(policyEdit.name))
            existingPolicy.name = policyEdit.name
        policyEdit.status?.let { existingPolicy.status = policyEdit.status }
        policyEdit.priority?.let {existingPolicy.priority = policyEdit.priority }
        PolicyValidator.validate(existingPolicy, false)

        try {
            matchPolicyApplicationChanges(policyId, existingPolicy, policyEdit)
            return savePolicy(existingPolicy, policyId)
        } catch (e: Exception) {
            throw NoSaveException(205002, "policy", e)
        }
    }

    fun deletePolicy(policyId: String) {
        val policies = searchPolicies(PolicySearchCriteria(policyIds = setOf(policyId), status = setOf(*EntityStatus.values())))
        if (policies.isEmpty())
            throw NoDeleteException(203001, "Policy", "Policy not found for deletion: ${policyId}")
        changePolicyStatuses(setOf(policyId), EntityStatus.DELETED)
    }

    open fun changePolicyStatuses(policyIds: Collection<String>, status: EntityStatus) {
        policyRepository.changePoliciesToStatus(policyIds, status)
    }

    /**
     * If subjects, resources, or permissions in an update to a policy are not null, this removes any persisted records
     * not present in the edit and sets the edited values to the current policy.
     */
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

    open fun savePolicy(policy: Policy, policyId: String? = null): Policy {
        val entities = PolicyMapper.dtoToEntities(policy,
                if (policyId == null) null else policyRepository.getOne(policyId))

        policyRepository.save(entities.policy)
        entities.actions.forEach{ policyActionRepository.save(it) }
        entities.subjects.forEach{ policySubjectRepository.save(it) }
        entities.resources.forEach{ policyResourceRepository.save(it) }

        return policy
    }

    fun verifyAccess(allowGuest: Boolean) {
        val grant = SessionContext.currentSession.get().grant
        if (!allowGuest && grant.type == AuthenticationType.GUEST)
            throw NoAccessException(201008, "You must be logged in to perform this action")
    }

}
