package net.nostalogic.access.sessions

import net.nostalogic.access.persistence.entities.ServerSessionEntity
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.security.models.SessionPrompt
import java.time.temporal.ChronoUnit

class PasswordResetSessionFactory(
    sessionPrompt: SessionPrompt
):
    SessionFactory(sessionPrompt) {

    init {
        sessionEnd = sessionStart.addTime(7, ChronoUnit.DAYS)
    }

    override fun createEntity(): ServerSessionEntity {
        return ServerSessionEntity(
            sessionId,
            userId,
            sessionStart.getTimestamp(),
            sessionEnd.getTimestamp(),
            AuthenticationType.PASSWORD_RESET,
            creatorId = userId
        )
    }

    override fun endOtherSessions(): Boolean {
        return true
    }
}
