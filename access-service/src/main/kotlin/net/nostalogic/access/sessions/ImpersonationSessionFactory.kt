package net.nostalogic.access.sessions

import net.nostalogic.access.persistence.entities.ServerSessionEntity
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.ExceptionCodes
import net.nostalogic.exceptions.NoAuthException
import net.nostalogic.security.models.SessionPrompt

class ImpersonationSessionFactory(
    sessionPrompt: SessionPrompt):
    SessionFactory(sessionPrompt) {

    val originalUserId: String

        init {
            refreshSessionEnd = accessSessionExpiration(sessionStart)
            originalUserId = sessionPrompt.originalUserId ?: throw NoAuthException(ExceptionCodes._0201011,
                "An impersonation session requires an original user ID")
            if (originalUserId == userId)
                throw NoAuthException(ExceptionCodes._0201012, "A user cannot impersonate themselves")

        }

    override fun createEntity(): ServerSessionEntity {
        return ServerSessionEntity(
            sessionId,
            userId,
            sessionStart.getTimestamp(),
            refreshSessionEnd.getTimestamp(),
            AuthenticationType.IMPERSONATION,
            creatorId = originalUserId,
            tenant = tenant,
        )
    }

    override fun getCreator(): String {
        return originalUserId
    }
}
