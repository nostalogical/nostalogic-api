package net.nostalogic.access.mappers

import net.nostalogic.access.datamodel.PolicyEntityComponents
import net.nostalogic.access.persistence.entities.PolicyActionEntity
import net.nostalogic.access.persistence.entities.PolicyEntity
import net.nostalogic.access.persistence.entities.PolicyResourceEntity
import net.nostalogic.access.persistence.entities.PolicySubjectEntity
import net.nostalogic.constants.Tenant
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.entities.EntityReference
import net.nostalogic.security.contexts.SessionContext
import net.nostalogic.utils.EntityUtils

object PolicyMapper {

    fun entityToDto(policyEntity: PolicyEntity,
                    resourceEntities: Collection<PolicyResourceEntity>,
                    subjectEntities: Collection<PolicySubjectEntity>,
                    actionEntities: Collection<PolicyActionEntity>): Policy {
        val policy = Policy(id = policyEntity.id, name = policyEntity.name,
                priority = policyEntity.priority, status = policyEntity.status, creator = policyEntity.creatorId)
        for (resource in resourceEntities)
            policy.resources?.add(EntityReference(resource.resourceId, resource.entity).toString())
        for (subject in subjectEntities)
            policy.subjects?.add(EntityReference(subject.subjectId, subject.entity).toString())
        for (action in actionEntities)
            policy.permissions?.set(action.action, action.allow)
        return policy
    }

    fun dtoToEntities(policy: Policy, existingEntity: PolicyEntity? = null, tenant: Tenant = Tenant.NOSTALOGIC): PolicyEntityComponents {
        val policyEntity: PolicyEntity = existingEntity ?: PolicyEntity(
            name = policy.name!!,
            priority = policy.priority!!,
            creatorId = policy.creator ?: SessionContext.getUserId(),
            tenant = tenant,
        )
        existingEntity?.let {
            policyEntity.name = policy.name!!
            policyEntity.priority = policy.priority!!
            policyEntity.status = policy.status!!
        }
        policy.status?.let { policyEntity.status = policy.status!! }
        val actionEntities: Collection<PolicyActionEntity> = if (policy.permissions == null) emptySet()
            else policy.permissions!!.map { (k, v) ->
                PolicyActionEntity(
                    policyId = policyEntity.id,
                    action = k,
                    allow = v,
                    tenant = tenant,
                ) }.toSet()

        val subjectEntities: Collection<PolicySubjectEntity> = if (policy.subjects == null) emptySet()
            else policy.subjects!!.map {
                val ref = EntityUtils.toEntityRef(it)
                PolicySubjectEntity(
                    policyId = policyEntity.id,
                    subjectId = ref.id,
                    entity = ref.entity,
                    tenant = tenant,
                ) }.toSet()

        val resourceEntities: Collection<PolicyResourceEntity> = if (policy.resources == null) emptySet()
            else policy.resources!!.map {
                val ref = EntityUtils.toEntityRef(it)
                PolicyResourceEntity(
                    policyId = policyEntity.id,
                    resourceId = ref.id,
                    entity = ref.entity,
                    tenant = tenant,
                    ) }.toSet()

        policy.id = policyEntity.id

        return PolicyEntityComponents(policyEntity, actionEntities, subjectEntities, resourceEntities)
    }

}
