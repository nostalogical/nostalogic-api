package net.nostalogic.access.persistence.entities

import net.nostalogic.access.services.AccessService
import net.nostalogic.constants.Tenant
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.persistence.entities.AbstractCoreEntity
import net.nostalogic.utils.EntityUtils
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity(name = "policy_action")
class PolicyActionEntity(
    val policyId: String,
    @Enumerated(EnumType.STRING) val action: PolicyAction,
    var allow: Boolean,
    creatorId: String = EntityUtils.SYSTEM_ID,
    tenant: Tenant,
) : AbstractCoreEntity(
    id = AccessService.compositeActionId(policyId, action),
    creatorId = creatorId,
    tenant = tenant,
)
