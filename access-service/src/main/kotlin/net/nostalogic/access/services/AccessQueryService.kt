package net.nostalogic.access.services

import net.nostalogic.access.models.PolicySearchCriteria
import net.nostalogic.access.persistence.repositories.PolicyActionRepository
import net.nostalogic.access.persistence.repositories.PolicyRepository
import net.nostalogic.access.persistence.repositories.PolicyResourceRepository
import net.nostalogic.access.persistence.repositories.PolicySubjectRepository
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoRetrieveException
import net.nostalogic.utils.EntityUtils
import org.springframework.stereotype.Service

@Service(value = "AccessQueryService")
open class AccessQueryService(
        private val policyRepository: PolicyRepository,
        private val policySubjectRepository: PolicySubjectRepository,
        private val policyResourceRepository: PolicyResourceRepository,
        private val policyActionRepository: PolicyActionRepository) {

    companion object {
        val SEARCH_PROPS = arrayOf("created", "id")
    }

//    fun evaluateAccessQuery(accessQuery: AccessQuery): AccessReport {
        // entity queries only requires searching for entity level resources
        // resource level queries
//    }

//    fun filterPolicies(criteria: PolicySearchCriteria): ArrayList<Policy> {
//        val filteredPolicyIds = HashSet<String>()
//
//        val filtered = criteria.policyIds.isNotEmpty()
//        filteredPolicyIds.addAll(criteria.policyIds)
//
//        if (criteria.subjectIds.isNotEmpty()) {
//            filteredPolicyIds.addAll(policySubjectRepository.findAllBySubjectIdIn(filterOnlyUuid(criteria.subjectIds)).map { s -> s.policyId })
//            if (criteria.subjectIds.contains(NoEntity.ALL.name))
//                filteredPolicyIds.addAll(policySubjectRepository.findAllBySubjectIdIsNull().map { s -> s.policyId })
//        }
//
//        if (criteria.resourceIds.isNotEmpty()) {
//            filteredPolicyIds.addAll(policyResourceRepository.findAllByResourceIdIn(filterOnlyUuid(criteria.resourceIds)).map { r -> r.policyId })
//            filteredPolicyIds.addAll(policyResourceRepository.findAllByEntityIn(filterOnlyEntity(criteria.resourceIds)).map { r -> r.policyId })
//        }
//
//        if (criteria.policyIds.isEmpty() && criteria.subjectIds.isEmpty() && criteria.resourceIds.isEmpty())
//            filteredPolicyIds.addAll(policyRepository.findAll().map { p -> p.id })
//
//        return retrievePolicies(filteredPolicyIds, criteria)
//    }

    fun getPolicy(policyId: String): Policy {
        val policies = searchPolicies(PolicySearchCriteria(policyIds = setOf(policyId), status = setOf(*EntityStatus.values())))
        if (policies.isEmpty())
            throw NoRetrieveException(204001, "Policy", "Policy ${policyId} not found in database")
        return policies[0]
    }

    /**
     * Inclusive search that returns all policies matching the supplied criteria. If no criteria area supplied all
     * policies are returned. If subject and resource IDs are supplied, all policies relating to those subjects and
     * resources are returned.
     */
    fun searchPolicies(criteria: PolicySearchCriteria): ArrayList<Policy> {
        val filteredPolicyIds = HashSet<String>()

        filteredPolicyIds.addAll(criteria.policyIds)

        if (criteria.subjectIds.isNotEmpty()) {
            filteredPolicyIds.addAll(policySubjectRepository.findAllBySubjectIdIn(filterOnlyUuid(criteria.subjectIds)).map { s -> s.policyId })
            if (criteria.subjectIds.contains(NoEntity.ALL.name))
                filteredPolicyIds.addAll(policySubjectRepository.findAllBySubjectIdIsNull().map { s -> s.policyId })
        }

        if (criteria.resourceIds.isNotEmpty()) {
            filteredPolicyIds.addAll(policyResourceRepository.findAllByResourceIdIn(filterOnlyUuid(criteria.resourceIds)).map { r -> r.policyId })
            filteredPolicyIds.addAll(policyResourceRepository.findAllByEntityIn(filterOnlyEntity(criteria.resourceIds)).map { r -> r.policyId })
        }

        if (criteria.policyIds.isEmpty() && criteria.subjectIds.isEmpty() && criteria.resourceIds.isEmpty())
            filteredPolicyIds.addAll(policyRepository.findAll().map { p -> p.id })

        return retrievePolicies(filteredPolicyIds, criteria)

    }

    private fun retrievePolicies(policyIds: Set<String>, criteria: PolicySearchCriteria): ArrayList<Policy> {
        val policyEntities = policyRepository.findAllByIdInAndStatusIn(policyIds, criteria.status, criteria.page.toQuery())
        criteria.page.hasNext = policyEntities.hasNext()
        val policiesById = HashMap<String, Policy>()
        val orderedPolicies = ArrayList<Policy>()
        for (policy in policyEntities) {
            policiesById[policy.id] = Policy(id = policy.id, name = policy.name, priority = policy.priority, status = policy.status)
            orderedPolicies.add(policiesById[policy.id]!!)
        }
        val resourceEntities = policyResourceRepository.findAllByPolicyIdIn(policyIds)
        for (resource in resourceEntities)
            policiesById[resource.policyId]?.resources?.add(EntityReference(resource.resourceId, resource.entity).toFullId())
        val subjectEntities = policySubjectRepository.findAllByPolicyIdIn(policyIds)
        for (subject in subjectEntities)
            policiesById[subject.policyId]?.subjects?.add(EntityReference(subject.subjectId, subject.entity).toFullId())
        val actionEntities = policyActionRepository.findAllByPolicyIdIn(policyIds)
        for (action in actionEntities)
            policiesById[action.policyId]?.permissions?.set(action.action, action.allow)

        return orderedPolicies
    }

    private fun filterOnlyUuid(entityIds: Collection<String>): Set<String> {
        val uuid = HashSet<String>()
        for (entityId in entityIds) {
            val ref = EntityUtils.toMaybeEntityRef(entityId)
            ref?.id?.let { uuid.add(it) }
        }
        return uuid
    }

    private fun filterOnlyEntity(entityIds: Collection<String>): Set<NoEntity> {
        val entities = HashSet<NoEntity>()
        for (entityId in entityIds) {
            if (EntityUtils.isEntity(entityId))
                entities.add(NoEntity.valueOf(entityId.toUpperCase()))
        }
        return entities
    }
}
