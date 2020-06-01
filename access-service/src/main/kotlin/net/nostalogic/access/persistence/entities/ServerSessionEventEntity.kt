package net.nostalogic.access.persistence.entities

import net.nostalogic.constants.SessionEvent
import net.nostalogic.datamodel.NoDate
import net.nostalogic.persistence.entities.AbstractJpaPersistable
import java.sql.Timestamp
import javax.persistence.Entity

@Entity(name = "server_session_event")
class ServerSessionEventEntity(
        val sessionId: String,
        sessionEvent: SessionEvent
) : AbstractJpaPersistable<Long>() {
    val event: String = sessionEvent.name
    val created: Timestamp = NoDate().getTimestamp()
}
