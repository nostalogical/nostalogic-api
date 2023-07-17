package net.nostalogic.access.persistence.entities

import net.nostalogic.constants.Tenant
import net.nostalogic.persistence.entities.AbstractCoreEntity
import net.nostalogic.utils.EntityUtils
import javax.persistence.Entity

@Entity(name = "access_extension")
class AccessExtensionEntity(
    val userId: String,
    val entityId: String,
    creatorId : String = EntityUtils.SYSTEM_ID,
    tenant: Tenant,
) : AbstractCoreEntity(
    creatorId =  creatorId,
    tenant = tenant,
)
