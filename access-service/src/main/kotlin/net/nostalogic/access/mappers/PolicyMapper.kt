package net.nostalogic.access.mappers

import net.nostalogic.access.persistence.entities.PolicyActionEntity
import net.nostalogic.access.persistence.entities.PolicyEntity
import net.nostalogic.access.persistence.entities.PolicyResourceEntity
import net.nostalogic.access.persistence.entities.PolicySubjectEntity
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.entities.EntityReference

object PolicyMapper {

    fun entityToDto(policyEntity: PolicyEntity,
                    resourceEntities: Collection<PolicyResourceEntity>,
                    subjectEntities: Collection<PolicySubjectEntity>,
                    actionEntities: Collection<PolicyActionEntity>): Policy {
        val policy = Policy(id = policyEntity.id, name = policyEntity.name,
                priority = policyEntity.priority, status = policyEntity.status)
        for (resource in resourceEntities)
            policy.resources?.add(EntityReference(resource.resourceId, resource.entity).toEntityReference())
        for (subject in subjectEntities)
            policy.subjects?.add(EntityReference(subject.subjectId, subject.entity).toEntityReference())
        for (action in actionEntities)
            policy.permissions?.set(action.action, action.allow)
        return policy
    }

}
