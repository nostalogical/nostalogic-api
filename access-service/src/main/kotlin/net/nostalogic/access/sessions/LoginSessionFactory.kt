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
            refreshSessionEnd = refreshSessionExpiration(sessionStart)
        }

    override fun createEntity(): ServerSessionEntity {
        return ServerSessionEntity(
            sessionId,
            userId,
            sessionStart.getTimestamp(),
            refreshSessionEnd.getTimestamp(),
            AuthenticationType.LOGIN,
            refreshKey = refreshKey,
            creatorId = userId,
            tenant = tenant,
        )
    }

    override fun getCreator(): String {
        return userId
    }

    override fun endOtherSessions(): Boolean {
        return sessionPrompt.reset
    }
}
