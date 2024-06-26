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
        refreshSessionEnd = sessionStart.addTime(7, ChronoUnit.DAYS)
    }

    override fun createEntity(): ServerSessionEntity {
        return ServerSessionEntity(
            sessionId,
            userId,
            sessionStart.getTimestamp(),
            refreshSessionEnd.getTimestamp(),
            AuthenticationType.PASSWORD_RESET,
            creatorId = userId,
            tenant = tenant,
        )
    }

    override fun getCreator(): String {
        return userId
    }

    override fun endOtherSessions(): Boolean {
        return true
    }
}
