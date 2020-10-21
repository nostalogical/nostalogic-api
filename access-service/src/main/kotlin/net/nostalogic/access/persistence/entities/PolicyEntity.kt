package net.nostalogic.access.persistence.entities

import net.nostalogic.datamodel.access.PolicyPriority
import net.nostalogic.entities.EntityStatus
import net.nostalogic.persistence.entities.AbstractCoreEntity
import net.nostalogic.utils.EntityUtils
import javax.persistence.Entity
import javax.persistence.Enumerated

@Entity(name = "policy")
class PolicyEntity(
        var name: String,
        @Enumerated var priority: PolicyPriority,
        creatorId: String = EntityUtils.SYSTEM_ID,
        @Enumerated var status: EntityStatus = EntityStatus.ACTIVE
): AbstractCoreEntity(creatorId = creatorId)
