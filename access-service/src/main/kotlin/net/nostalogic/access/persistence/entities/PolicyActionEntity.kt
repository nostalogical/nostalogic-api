package net.nostalogic.access.persistence.entities

import net.nostalogic.access.services.AccessService
import net.nostalogic.datamodel.access.PolicyAction
import java.sql.Timestamp
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id

@Entity(name = "policy_action")
class PolicyActionEntity(
        val policyId: String,
        @Enumerated(EnumType.STRING) val action: PolicyAction,
        var allow: Boolean,
        @Id val id: String = AccessService.compositeActionId(policyId, action),
        val created: Timestamp = Timestamp.from(Instant.now())
)
