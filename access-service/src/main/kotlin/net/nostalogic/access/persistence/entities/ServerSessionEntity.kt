package net.nostalogic.access.persistence.entities

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.Tenant
import net.nostalogic.persistence.entities.AbstractCoreEntity
import net.nostalogic.utils.EntityUtils
import java.sql.Timestamp
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity(name = "server_session")
class ServerSessionEntity(
        id: String,
        val userId: String,
        val startDateTime: Timestamp,
        var endDateTime: Timestamp,
        @Enumerated(EnumType.STRING) val type: AuthenticationType,
        var refreshKey: String? = null,
        val notes: String? = null,
        creatorId : String = EntityUtils.SYSTEM_ID,
        tenant: Tenant,
) : AbstractCoreEntity(
        id = id,
        creatorId =  creatorId,
        tenant = tenant,
)
