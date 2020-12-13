package net.nostalogic.access.services

import net.nostalogic.access.persistence.entities.ServerSessionEntity
import net.nostalogic.access.persistence.entities.ServerSessionEventEntity
import net.nostalogic.access.persistence.repositories.ServerSessionEventRepository
import net.nostalogic.access.persistence.repositories.ServerSessionRepository
import net.nostalogic.config.Config
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.SessionEvent
import net.nostalogic.datamodel.NoDate
import net.nostalogic.exceptions.NoAuthException
import net.nostalogic.security.grants.ImpersonationGrant
import net.nostalogic.security.grants.LoginGrant
import net.nostalogic.security.grants.PasswordResetGrant
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.security.models.SessionSummary
import net.nostalogic.security.utils.TokenDecoder
import net.nostalogic.security.utils.TokenEncoder
import net.nostalogic.utils.EntityUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class SessionService(
        private val sessionRepo: ServerSessionRepository,
        private val eventRepo: ServerSessionEventRepository) {

    private val expirationKey = "security.jwt.duration-minutes"

    fun createSession(prompt: SessionPrompt): SessionSummary {
        if (StringUtils.isBlank(prompt.userId))
            throw throw NoAuthException(201007, "Cannot create a session without a user ID")
        val start = NoDate()
        val end = standardExpiration(start)

        val persisted: ServerSessionEntity = if (prompt.type == AuthenticationType.USERNAME || prompt.type == AuthenticationType.EMAIL) {
            val grant = LoginGrant(prompt.userId, prompt.additional, end,
                    EntityUtils.uuid(), prompt.type, created = start)
            val entity = ServerSessionEntity(grant.sessionId, grant.subject, grant.additional.joinToString(","),
                    grant.created.getTimestamp(), grant.expiration!!.getTimestamp(), grant.type, grant.description, creatorId = prompt.userId)
            sessionRepo.save(entity)
        } else if (prompt.type == AuthenticationType.IMPERSONATION) {
            val grant = ImpersonationGrant(prompt.userId, prompt.additional, end,
                    EntityUtils.uuid(), prompt.originalUserId!!, prompt.additional, created = start)
            val entity = ServerSessionEntity(grant.sessionId, grant.subject, grant.additional.joinToString(","),
                    grant.created.getTimestamp(), grant.expiration!!.getTimestamp(), grant.type, grant.description, creatorId = prompt.originalUserId!!)
            sessionRepo.save(entity)
        } else if (prompt.type == AuthenticationType.PASSWORD_RESET) {
            val grant = PasswordResetGrant(prompt.userId, created = start)
            val entity = ServerSessionEntity(EntityUtils.uuid(), grant.subject, prompt.additional.joinToString(","),
                    start.getTimestamp(), end.getTimestamp(), grant.type, grant.description, creatorId = prompt.userId)
            sessionRepo.save(entity)
        } else
            throw NoAuthException(201009, "Authentication type is not supported")

        addSessionEvent(ServerSessionEventEntity(persisted.id, SessionEvent.LOGIN))
        if (prompt.reset) {
            addSessionEvent(ServerSessionEventEntity(persisted.id, SessionEvent.RESET))
            endOtherSessions(persisted.userId, persisted)
        }
        return summaryFromSession(persisted, alternates = prompt.alternates)
    }

    fun verifySession(token: String): SessionSummary {
        return summaryFromSession(retrieveSessionFromToken(token))
    }

    fun refreshSession(token: String): SessionSummary {
        val session = retrieveSessionFromToken(token)
        if (isExpired(session.endDateTime))
            throw NoAuthException(201005, "Session has already expired and cannot be refreshed")
        session.endDateTime = standardExpiration().getTimestamp()
        val updated = sessionRepo.save(session)
        addSessionEvent(ServerSessionEventEntity(updated.id, SessionEvent.REFRESH))
        return summaryFromSession(updated)
    }

    fun updateUserSessions(userId: String, groups: Set<String>) {
        val userSessions = sessionRepo.findAllByUserIdAndEndDateTimeIsAfter(userId, Timestamp.from(Instant.now()))
        val currentGroups = if (userSessions.isEmpty()) emptySet<String>() else HashSet(userSessions.first().additional!!.split(","))
        if (currentGroups != groups) {
            for (session in userSessions) {
                session.additional = groups.joinToString(",")
                sessionRepo.save(session)
                addSessionEvent(ServerSessionEventEntity(session.id, SessionEvent.GROUPS_CHANGE))
            }
        }
    }

    fun expireSession(token: String): SessionSummary {
        val session = retrieveSessionFromToken(token)
        if (isExpired(session.endDateTime))
            throw NoAuthException(201006, "Session has already ended")
        session.endDateTime = Timestamp.from(Instant.now().minusSeconds(5))
        val updated = sessionRepo.save(session)
        addSessionEvent(ServerSessionEventEntity(updated.id, SessionEvent.LOGOUT))
        return summaryFromSession(updated, requiredValid = false)
    }

    private fun retrieveSessionFromToken(token: String): ServerSessionEntity {
        val grant = TokenDecoder.decodeToken(token)
        val session: ServerSessionEntity?
        when (grant.type) {
            AuthenticationType.USERNAME, AuthenticationType.EMAIL -> session = getSessionById((grant as LoginGrant).sessionId)
            else -> throw NoAuthException(201002, "Session type ${grant.type.name} is not currently supported")
        }
        if (session == null)
            throw NoAuthException(201003, "Supplied token does not match a database entry: $token")
        return session
    }

    private fun getSessionById(sessionId: String): ServerSessionEntity? {
        return sessionRepo.findByIdOrNull(sessionId)
    }

    private fun addSessionEvent(event: ServerSessionEventEntity) {
        eventRepo.save(event)
    }

    private fun summaryFromSession(entity: ServerSessionEntity, requiredValid: Boolean = true, alternates: Set<String>? = null): SessionSummary {
        if (requiredValid && isExpired(entity.endDateTime))
            throw NoAuthException(201004, "Server session has expired")
        return SessionSummary(entity.id, entity.userId, entity.type,
                NoDate(entity.startDateTime), NoDate(entity.endDateTime), entity.details, tokenFromSession(entity, alternates))
    }

    private fun tokenFromSession(entity: ServerSessionEntity, alternates: Set<String>? = null): String {
        return when (entity.type) {
            AuthenticationType.EMAIL, AuthenticationType.USERNAME, AuthenticationType.PASSWORD_RESET -> loginTokenFromSession(entity)
            AuthenticationType.IMPERSONATION -> impersonationTokenFromSession(entity, alternates!!)
            else -> throw NoAuthException(201001, "Session type ${entity.type.name} is not supported")
        }
    }

    private fun endOtherSessions(userId: String, exclude: ServerSessionEntity?) {
        val sessions = sessionRepo.findAllByUserIdAndEndDateTimeIsAfter(userId, Timestamp(System.currentTimeMillis()))
        sessions.removeIf { it.id ==  exclude?.id }
        val endTime = Timestamp.from(Instant.now().minusSeconds(5))
        sessions.forEach { if (it.endDateTime.after(endTime)) it.endDateTime = endTime }
        if (sessions.isNotEmpty())
            sessionRepo.saveAll(sessions)
    }

    private fun loginTokenFromSession(entity: ServerSessionEntity): String {
        val grant = LoginGrant(
                entity.userId,
                entity.additional?.split(",")!!.toSet(),
                NoDate(entity.endDateTime),
                entity.id,
                entity.type,
                NoDate(entity.startDateTime))
        return TokenEncoder.encodeLoginGrant(grant)
    }

    private fun impersonationTokenFromSession(entity: ServerSessionEntity, alternates: Set<String>): String {
        val grant = ImpersonationGrant(
                entity.userId,
                entity.additional?.split(",")!!.toSet(),
                NoDate(entity.endDateTime),
                entity.id,
                entity.creatorId,
                alternates,
                NoDate(entity.startDateTime))
        return TokenEncoder.encodeImpersonationGrant(grant)
    }

    private fun isExpired(time: Timestamp): Boolean {
        return time.before(Timestamp.from(Instant.now()))
    }

    private fun standardExpiration(from: NoDate = NoDate()): NoDate {
        return from.addTime(Config.getNumberSetting(expirationKey).toLong(), ChronoUnit.MINUTES)
    }
}
