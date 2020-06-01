package net.nostalogic.access.persistence.entities

import net.nostalogic.constants.AuthenticationType
import java.sql.Timestamp
import javax.persistence.Entity
import javax.persistence.Id

@Entity(name = "server_session")
class ServerSessionEntity(
        @Id val sessionId: String,
        val userId: String,
        var additional: String?,
        val startDateTime: Timestamp,
        var endDateTime: Timestamp,
        val type: AuthenticationType,
        val details: String?
)
