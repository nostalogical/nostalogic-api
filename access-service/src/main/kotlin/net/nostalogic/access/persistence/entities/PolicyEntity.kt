package net.nostalogic.access.persistence.entities

import net.nostalogic.constants.Tenant
import net.nostalogic.datamodel.access.PolicyPriority
import net.nostalogic.entities.EntityStatus
import net.nostalogic.persistence.entities.AbstractCoreEntity
import net.nostalogic.utils.EntityUtils
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity(name = "policy")
class PolicyEntity(
        var name: String,
        @Enumerated(EnumType.STRING) var priority: PolicyPriority,
        creatorId: String = EntityUtils.SYSTEM_ID,
        @Enumerated(EnumType.STRING) var status: EntityStatus = EntityStatus.ACTIVE,
        tenant: Tenant,
): AbstractCoreEntity(
        creatorId = creatorId,
        tenant = tenant,
)
