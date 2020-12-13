package net.nostalogic.access.services

import net.nostalogic.access.datamodel.PolicySearchCriteria
import net.nostalogic.access.datamodel.ResourcePermissionContext
import net.nostalogic.access.persistence.repositories.PolicyActionRepository
import net.nostalogic.access.persistence.repositories.PolicyRepository
import net.nostalogic.access.persistence.repositories.PolicyResourceRepository
import net.nostalogic.access.persistence.repositories.PolicySubjectRepository
import net.nostalogic.datamodel.NoPageable
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.AccessReport
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoRetrieveException
import net.nostalogic.utils.EntityUtils
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

@Service
open class AccessQueryService(
        private val policyRepository: PolicyRepository,
        private val policySubjectRepository: PolicySubjectRepository,
        private val policyResourceRepository: PolicyResourceRepository,
        private val policyActionRepository: PolicyActionRepository) {

    fun evaluateAccessQuery(accessQuery: AccessQuery): AccessReport {
        val contexts = analyseAccessQuery(accessQuery)
        val resourcePermissions = HashMap<String, HashMap<PolicyAction, Boolean>>()
        val entityPermissions = HashMap<NoEntity, HashMap<PolicyAction, Boolean>>()
        for (context in contexts) {
            if (context.resource.isSignature())
                resourcePermissions.getOrPut(context.resource.toString()) { HashMap() }[context.action] = context.allow
            else
                entityPermissions.getOrPut(context.resource.entity) { HashMap() }[context.action] = context.allow
        }
        return AccessReport(accessQuery.subjects, resourcePermissions, entityPermissions)
    }

    fun analyseAccessQuery(accessQuery: AccessQuery): Collection<ResourcePermissionContext> {
        val subjects = accessQuery.subjects.map { EntityUtils.toEntityRef(it) }
        val subjectIds = subjects.filter { it.isSignature() }.map { it.id!! }.toHashSet()
        val resources = accessQuery.resourceQueries.keys.map { EntityUtils.toEntityRef(it) }
        val resourceIds = resources.filter { it.isSignature() }.map { it.id!! }.toHashSet()
        val resourceEntities = resources.map { it.entity }.toHashSet()
        resourceEntities.add(NoEntity.ALL)
        val policyIds = policyRepository.findPolicyIdsForSubjectsAndResources(allEntity = NoEntity.ALL,
                resourceEntities = resourceEntities, resourceIds = resourceIds, subjectIds = subjectIds)
        val policies = ArrayList<Policy>()
        var pageNum = 0
        do {
            val criteria = PolicySearchCriteria(status = setOf(EntityStatus.ACTIVE), page = NoPageable(page = ++pageNum, sortFields = *PolicySearchCriteria.DEFAULT_SORT_FIELDS))
            policies.addAll(retrievePolicies(policyIds, criteria))
        } while (criteria.page.hasNext == true)

        val resourceContexts = HashMap<EntityReference, EnumMap<PolicyAction, ResourcePermissionContext>>()
        accessQuery.resourceQueries.forEach { (k, v) -> run {
            val ref = EntityUtils.toEntityRef(k)
            resourceContexts[ref] = EnumMap(PolicyAction::class.java)
            v.forEach { resourceContexts[ref]!![it] = ResourcePermissionContext(ref, it) }
        } }

        val querySubjects = accessQuery.subjects.toHashSet()
        querySubjects.add(NoEntity.ALL.name)
        for (policy in policies) {
            val relevantSubjects = policy.subjects!!.toHashSet()
            relevantSubjects.retainAll(querySubjects)
            for (policyResource in policy.resources!!) {
                for (queryResource in resources) {
                    if (queryResource.toString() == policyResource || queryResource.entity.name == policyResource || policyResource == NoEntity.ALL.name) {
                        for (policyAction in policy.permissions!!.keys) {
                            if (resourceContexts[queryResource]!!.keys.contains(policyAction))
                                resourceContexts[queryResource]!![policyAction]!!.updatePolicies(policy, relevantSubjects, policyResource, policyAction)
                        }
                    }
                }
            }
        }

        return resourceContexts.flatMap { it.value.values }
    }

    /**
     * Exclusive search that returns all policies matching each and every one of the supplied criteria, or all policies
     * if no criteria are supplied. If subject and resource IDs are supplied, only policies matching all the supplied
     * criteria are returned.
     */
    fun filterPolicies(criteria: PolicySearchCriteria): ArrayList<Policy> {
        if (criteria.subjectIds.isEmpty() && criteria.resourceIds.isEmpty())
            return retrievePolicies(policyRepository.findAll().map { p -> p.id }.toHashSet(), criteria)

        val policiesBySubject = HashSet<String>()
        val policiesByResource = HashSet<String>()
        addPoliciesBySubjectCriteria(criteria, policiesBySubject)
        addPoliciesByResourceCriteria(criteria, policiesByResource)

        val filteredPolicyIds = if (criteria.subjectIds.isNotEmpty()) policiesBySubject else policiesByResource
        if (criteria.subjectIds.isNotEmpty() && criteria.resourceIds.isNotEmpty())
            filteredPolicyIds.retainAll(policiesByResource)

        val filteredPolicies = retrievePolicies(filteredPolicyIds, criteria)
        if (criteria.actions.size != PolicyAction.values().size)
            filteredPolicies.removeIf {
                var containsAction = false
                for (action in criteria.actions) {
                    if (it.permissions!!.containsKey(action))
                        containsAction = true
                }
                !containsAction
            }

        return filteredPolicies
    }

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
        if (criteria.policyIds.isEmpty() && criteria.subjectIds.isEmpty() && criteria.resourceIds.isEmpty())
            return retrievePolicies(policyRepository.findAll().map { p -> p.id }.toHashSet(), criteria)

        val filteredPolicyIds = HashSet<String>()
        filteredPolicyIds.addAll(criteria.policyIds)
        addPoliciesBySubjectCriteria(criteria, filteredPolicyIds)
        addPoliciesByResourceCriteria(criteria, filteredPolicyIds)

        return retrievePolicies(filteredPolicyIds, criteria)
    }

    private fun addPoliciesBySubjectCriteria(criteria: PolicySearchCriteria, aggregate: HashSet<String>) {
        if (criteria.subjectIds.isNotEmpty()) {
            aggregate.addAll(policySubjectRepository.findAllBySubjectIdIn(filterOnlyUuid(criteria.subjectIds)).map { s -> s.policyId })
            if (criteria.subjectIds.contains(NoEntity.ALL.name))
                aggregate.addAll(policySubjectRepository.findAllBySubjectIdIsNull().map { s -> s.policyId })
        }
    }

    private fun addPoliciesByResourceCriteria(criteria: PolicySearchCriteria, aggregate: HashSet<String>) {
        if (criteria.resourceIds.isNotEmpty()) {
            aggregate.addAll(policyResourceRepository.findAllByResourceIdIn(filterOnlyUuid(criteria.resourceIds)).map { r -> r.policyId })
            aggregate.addAll(policyResourceRepository.findAllByEntityIn(filterOnlyEntity(criteria.resourceIds)).map { r -> r.policyId })
        }
    }

    private fun addPoliciesByActionCriteria(criteria: PolicySearchCriteria, aggregate: HashSet<String>) {
        if (criteria.actions.isNotEmpty())
            aggregate.addAll(policyActionRepository.findAllByActionIn(criteria.actions).map { a -> a.policyId })
    }

    private fun retrievePolicies(policyIds: Set<String>, criteria: PolicySearchCriteria): ArrayList<Policy> {
        val policyEntities = policyRepository.findAllByIdInAndStatusInAndPriorityIn(policyIds, criteria.status, criteria.priority, criteria.page.toQuery())
        criteria.page.hasNext = policyEntities.hasNext()
        val policiesById = HashMap<String, Policy>()
        val orderedPolicies = ArrayList<Policy>()
        for (policy in policyEntities) {
            policiesById[policy.id] = Policy(id = policy.id, name = policy.name, priority = policy.priority, status = policy.status, creator = policy.creatorId)
            orderedPolicies.add(policiesById[policy.id]!!)
        }
        val resourceEntities = policyResourceRepository.findAllByPolicyIdIn(policyIds)
        for (resource in resourceEntities)
            policiesById[resource.policyId]?.resources?.add(EntityReference(resource.resourceId, resource.entity).toString())
        val subjectEntities = policySubjectRepository.findAllByPolicyIdIn(policyIds)
        for (subject in subjectEntities)
            policiesById[subject.policyId]?.subjects?.add(EntityReference(subject.subjectId, subject.entity).toString())
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
