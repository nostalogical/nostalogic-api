package net.nostalogic.access.persistence.entities

import net.nostalogic.access.services.AccessService
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity
import java.sql.Timestamp
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id

@Entity(name = "policy_resource")
class PolicyResourceEntity(
        val policyId: String,
        val resourceId: String?,
        @Enumerated(EnumType.STRING) val entity: NoEntity,
        val created: Timestamp = Timestamp.from(Instant.now()),
        @Id val id: String = AccessService.compositePolicyId(policyId, EntityReference(resourceId, entity))
)
