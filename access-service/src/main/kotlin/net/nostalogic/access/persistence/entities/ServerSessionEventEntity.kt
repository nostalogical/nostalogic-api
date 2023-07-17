package net.nostalogic.access.persistence.entities

import net.nostalogic.constants.SessionEvent
import net.nostalogic.constants.Tenant
import net.nostalogic.persistence.entities.AbstractCoreEntity
import net.nostalogic.utils.EntityUtils
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity(name = "server_session_event")
class ServerSessionEventEntity(
    val sessionId: String,
    @Enumerated(EnumType.STRING) val sessionEvent: SessionEvent,
    creatorId : String = EntityUtils.SYSTEM_ID,
    tenant: Tenant,
) : AbstractCoreEntity(
    creatorId =  creatorId,
    tenant = tenant,
)
