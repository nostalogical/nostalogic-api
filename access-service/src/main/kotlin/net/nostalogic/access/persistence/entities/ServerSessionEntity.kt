package net.nostalogic.access.persistence.entities

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.persistence.entities.AbstractCoreEntity
import net.nostalogic.utils.EntityUtils
import java.sql.Timestamp
import javax.persistence.Entity

@Entity(name = "server_session")
class ServerSessionEntity(
        id: String,
        val userId: String,
        var additional: String?,
        val startDateTime: Timestamp,
        var endDateTime: Timestamp,
        val type: AuthenticationType,
        val details: String?,
        creatorId : String = EntityUtils.SYSTEM_ID
) : AbstractCoreEntity(id = id, creatorId =  creatorId)
