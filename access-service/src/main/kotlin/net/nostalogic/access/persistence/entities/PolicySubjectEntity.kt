package net.nostalogic.access.persistence.entities

import net.nostalogic.access.services.AccessService
import net.nostalogic.constants.Tenant
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity
import net.nostalogic.persistence.entities.AbstractCoreEntity
import net.nostalogic.utils.EntityUtils
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity(name = "policy_subject")
class PolicySubjectEntity(
        val policyId: String,
        val subjectId: String?,
        @Enumerated(EnumType.STRING) val entity: NoEntity,
        creatorId: String = EntityUtils.SYSTEM_ID,
        tenant: Tenant,
): AbstractCoreEntity(
        id = AccessService.compositePolicyId(policyId, EntityReference(subjectId, entity)),
        creatorId = creatorId,
        tenant = tenant,
)
