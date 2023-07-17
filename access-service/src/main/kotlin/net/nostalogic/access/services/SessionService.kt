package net.nostalogic.access.services

import net.nostalogic.access.persistence.entities.AccessExtensionEntity
import net.nostalogic.access.persistence.entities.ServerSessionEntity
import net.nostalogic.access.persistence.entities.ServerSessionEventEntity
import net.nostalogic.access.persistence.repositories.AccessExtensionRepository
import net.nostalogic.access.persistence.repositories.ServerSessionEventRepository
import net.nostalogic.access.persistence.repositories.ServerSessionRepository
import net.nostalogic.access.sessions.SessionFactory
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.ExceptionCodes._0101009
import net.nostalogic.constants.ExceptionCodes._0201001
import net.nostalogic.constants.ExceptionCodes._0201002
import net.nostalogic.constants.ExceptionCodes._0201003
import net.nostalogic.constants.ExceptionCodes._0201004
import net.nostalogic.constants.ExceptionCodes._0201005
import net.nostalogic.constants.ExceptionCodes._0201006
import net.nostalogic.constants.ExceptionCodes._0201008
import net.nostalogic.constants.ExceptionCodes._0201010
import net.nostalogic.constants.ExceptionCodes._0201013
import net.nostalogic.constants.SessionEvent
import net.nostalogic.constants.Tenant
import net.nostalogic.crypto.encoders.EncoderType
import net.nostalogic.crypto.encoders.HexEncoder
import net.nostalogic.crypto.encoders.PasswordEncoder
import net.nostalogic.datamodel.NoDate
import net.nostalogic.datamodel.authentication.EncodingDetails
import net.nostalogic.datamodel.authentication.UserAuthentication
import net.nostalogic.exceptions.NoAccessException
import net.nostalogic.exceptions.NoAuthException
import net.nostalogic.security.grants.*
import net.nostalogic.security.models.GroupsAccessUpdate
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.security.models.SessionSummary
import net.nostalogic.security.models.TokenDetails
import net.nostalogic.security.utils.TokenDecoder
import net.nostalogic.security.utils.TokenEncoder
import net.nostalogic.utils.EntityUtils
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

@Service
class SessionService(
        private val sessionRepo: ServerSessionRepository,
        private val eventRepo: ServerSessionEventRepository,
        private val extensionRepo: AccessExtensionRepository,
) {

    fun createSession(prompt: SessionPrompt): SessionSummary {
        val sessionFactory = SessionFactory.create(prompt)
        val persistedSession = sessionRepo.save(sessionFactory.createEntity())

        addSessionEvent(ServerSessionEventEntity(
            persistedSession.id,
            SessionEvent.BEGIN,
            creatorId = sessionFactory.getCreator(),
            tenant = sessionFactory.tenant,
            ))
        if (sessionFactory.endOtherSessions()) {
            addSessionEvent(ServerSessionEventEntity(
                persistedSession.id,
                SessionEvent.LOGOUT_OTHERS,
                creatorId = sessionFactory.getCreator(),
                tenant = sessionFactory.tenant,
            ))
            endOtherSessions(prompt.userId, persistedSession)
        }

        confirmSessionIsValid(persistedSession)
        return summaryFromSession(persistedSession, includeTokens = true)
    }

    fun verifySession(token: String): SessionSummary {
        val grant = TokenDecoder.decodeToken(token)
        if (grant.type != AuthenticationType.LOGIN && grant.type != AuthenticationType.IMPERSONATION)
            throw NoAuthException(_0201010, "The ${grant.type} token type cannot be verified")

        val sessionEntity = retrieveSessionFromToken(grant)
        confirmSessionIsValid(sessionEntity)
        return summaryFromSession(sessionEntity, includeTokens = false)
    }

    fun refreshSession(token: String): SessionSummary {
        val grant = TokenDecoder.decodeToken(token)
        if (grant.type != AuthenticationType.REFRESH || grant !is RefreshGrant)
            throw NoAuthException(_0201008, "A refresh token is required to refresh a session")
        val session = retrieveSessionFromToken(grant)
        if (isExpired(session.endDateTime))
            throw NoAuthException(_0201005, "Session has already expired and cannot be refreshed")
        val verified = PasswordEncoder.verifyPassword(
            UserAuthentication(
                password = session.refreshKey!!,
                hash = grant.refreshHash,
                salt = HexEncoder.bytesToHex(session.id.toByteArray()),
                iterations = 50,
                encoder = EncoderType.PBKDF2,
            )
        )
        if (!verified)
            throw NoAuthException(_0201013, "Refresh token is invalid")
        session.endDateTime = SessionFactory.refreshSessionExpiration().getTimestamp()
        session.refreshKey = EntityUtils.uuid()
        val updated = sessionRepo.save(session)
        addSessionEvent(ServerSessionEventEntity(
            sessionId = updated.id,
            sessionEvent = SessionEvent.REFRESH,
            creatorId = session.creatorId,
            tenant = session.tenant,
        ))
        confirmSessionIsValid(updated)
        return summaryFromSession(updated, includeTokens = true)
    }

    /**
     * A user inherits access rights from groups they're part of, so their access group records must be updated each
     * time their group memberships change to add/remove access. This update is expected to be infrequent, so it should
     * contain an exhaustive list of the user's groups, meaning they can be reset and replaced with the provided list.
     */
    fun updateUserGroups(userId: String, update: GroupsAccessUpdate) {
        val tenant = Tenant.fromName(update.tenant) ?: throw NoAccessException(_0101009,
            "User groups cannot be updated, no valid tenant is specified")
        val currentExtensions = extensionRepo.findAllByUserId(userId)
        val currentGroups = currentExtensions.map { it.entityId }.toHashSet()
        extensionRepo.deleteAllByUserId(userId)
        val updatedExtensions = update.groups.map {
            AccessExtensionEntity(
                userId = userId,
                entityId = it,
                creatorId = update.updaterId ?: EntityUtils.SYSTEM_ID,
                tenant = tenant,
            )
        }
        extensionRepo.saveAll(updatedExtensions)

        val userSessions = sessionRepo.findAllByUserIdAndEndDateTimeIsAfter(userId, Timestamp.from(Instant.now()))
        if (currentGroups != update.groups) {
            for (session in userSessions) {
                addSessionEvent(ServerSessionEventEntity(
                    sessionId = session.id,
                    sessionEvent = SessionEvent.GROUPS_CHANGE,
                    tenant = session.tenant,
                    ))
            }
        }
    }

    fun expireSession(token: String): SessionSummary {
        val session = retrieveSessionFromToken(TokenDecoder.decodeToken(token))
        if (isExpired(session.endDateTime))
            throw NoAuthException(_0201006, "Session has already ended")
        session.endDateTime = Timestamp.from(Instant.now().minusSeconds(5))
        val updated = sessionRepo.save(session)
        addSessionEvent(ServerSessionEventEntity(
            sessionId = updated.id,
            sessionEvent = SessionEvent.LOGOUT,
            tenant = session.tenant,
            ))
        return summaryFromSession(updated, includeTokens = false)
    }

    private fun retrieveSessionFromToken(grant: NoGrant): ServerSessionEntity {
        val session: ServerSessionEntity = when (grant.type) {
            AuthenticationType.LOGIN -> getSessionById((grant as LoginGrant).sessionId)
            AuthenticationType.REFRESH -> getSessionById((grant as RefreshGrant).sessionId)
            else -> throw NoAuthException(_0201002, "Session type ${grant.type.name} is not currently supported")
        } ?: throw NoAuthException(_0201003, "Supplied token does not match a database entry")
        return session
    }

    private fun getSessionById(sessionId: String): ServerSessionEntity? {
        return sessionRepo.findById(sessionId).orElse(null)
    }

    private fun addSessionEvent(event: ServerSessionEventEntity) {
        eventRepo.save(event)
    }

    private fun confirmSessionIsValid(entity: ServerSessionEntity) {
        if (isExpired(entity.endDateTime))
            throw NoAuthException(_0201004, "Server session has expired")
    }

    private fun summaryFromSession(entity: ServerSessionEntity, includeTokens: Boolean): SessionSummary {
        val accessToken: TokenDetails? = if (includeTokens) accessTokenForSession(entity) else null
        val refreshToken: TokenDetails? = if (includeTokens) refreshTokenForSession(entity) else null

        return SessionSummary(
            entity.id,
            entity.userId,
            entity.type,
            NoDate(entity.startDateTime),
            NoDate(entity.endDateTime),
            entity.notes,
            accessToken,
            refreshToken,
        )
    }

    private fun endOtherSessions(userId: String, exclude: ServerSessionEntity?) {
        val sessions = sessionRepo.findAllByUserIdAndEndDateTimeIsAfter(userId, Timestamp(System.currentTimeMillis()))
        sessions.removeIf { it.id ==  exclude?.id }
        val endTime = Timestamp.from(Instant.now().minusSeconds(5))
        sessions.forEach { if (it.endDateTime.after(endTime)) it.endDateTime = endTime }
        val sessionLogouts = sessions.map { ServerSessionEventEntity(
            sessionId = it.id,
            sessionEvent = SessionEvent.FORCE_ENDED,
            creatorId = exclude?.creatorId ?: EntityUtils.SYSTEM_ID,
            tenant = it.tenant,
            )
        }
        eventRepo.saveAll(sessionLogouts)
        if (sessions.isNotEmpty())
            sessionRepo.saveAll(sessions)
    }

    private fun accessTokenForSession(entity: ServerSessionEntity): TokenDetails {
        return when (entity.type) {
            AuthenticationType.LOGIN, AuthenticationType.PASSWORD_RESET -> createLoginAccessToken(entity)
            AuthenticationType.IMPERSONATION -> createImpersonationAccessToken(entity)
            else -> throw NoAuthException(_0201001, "Session type ${entity.type.name} is not supported")
        }
    }

    private fun createLoginAccessToken(entity: ServerSessionEntity): TokenDetails {
        val grant = LoginGrant(
            subject = entity.userId,
            expiration = SessionFactory.accessSessionExpiration(NoDate(entity.startDateTime)),
            sessionId = entity.id,
            created = NoDate(entity.startDateTime),
            tenant = entity.tenant.name,
        )
        return TokenDetails(TokenEncoder.encodeLoginGrant(grant), grant.expiration)
    }

    private fun createImpersonationAccessToken(entity: ServerSessionEntity): TokenDetails {
        val grant = ImpersonationGrant(
            subject = entity.userId,
            expiration = NoDate(entity.endDateTime),
            sessionId = entity.id,
            originalSubject = entity.creatorId,
            created = NoDate(entity.startDateTime),
            tenant = entity.tenant.name,
        )
        return TokenDetails(TokenEncoder.encodeImpersonationGrant(grant), grant.expiration)
    }

    private fun refreshTokenForSession(entity: ServerSessionEntity): TokenDetails? {
        if (entity.refreshKey == null) return null

        val hashedRefresh = PasswordEncoder.encodePassword(
            EncodingDetails(
                password = entity.refreshKey!!,
                salt = entity.id,
                iterations = 50
            ),
            EncoderType.PBKDF2)
        val grant = RefreshGrant(
            subject = entity.userId,
            expiration = NoDate(entity.endDateTime),
            sessionId = entity.id,
            refreshHash = hashedRefresh.hash,
            created = NoDate(entity.startDateTime),
            tenant = entity.tenant.name,
        )
        return TokenDetails(TokenEncoder.encodeRefreshGrant(grant), grant.expiration)
    }

    private fun isExpired(time: Timestamp): Boolean {
        return time.before(Timestamp.from(Instant.now()))
    }
}
