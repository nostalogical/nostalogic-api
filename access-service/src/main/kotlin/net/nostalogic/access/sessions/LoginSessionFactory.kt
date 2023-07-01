package net.nostalogic.access.sessions

import net.nostalogic.access.persistence.entities.ServerSessionEntity
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.utils.EntityUtils

class LoginSessionFactory(
    sessionPrompt: SessionPrompt
): SessionFactory(sessionPrompt) {

    private val refreshKey: String = EntityUtils.uuid()

        init {
            sessionEnd = standardSessionExpiration(sessionStart)
        }

    override fun createEntity(): ServerSessionEntity {
        return ServerSessionEntity(
            sessionId,
            userId,
            sessionStart.getTimestamp(),
            sessionEnd.getTimestamp(),
            AuthenticationType.LOGIN,
            refreshKey = refreshKey,
            creatorId = userId,
        )
    }

    override fun endOtherSessions(): Boolean {
        return sessionPrompt.reset
    }
}
