package net.nostalogic.access.services

import io.mockk.*
import net.nostalogic.access.config.AccessTestConfig
import net.nostalogic.access.persistence.entities.ServerSessionEntity
import net.nostalogic.access.persistence.entities.ServerSessionEventEntity
import net.nostalogic.access.persistence.repositories.ServerSessionEventRepository
import net.nostalogic.access.persistence.repositories.ServerSessionRepository
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.SessionEvent
import net.nostalogic.exceptions.NoAuthException
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.security.utils.TokenDecoder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@ActiveProfiles(profiles = ["test"])
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AccessTestConfig::class])
class SessionServiceTest(
        @Autowired val sessionService: SessionService,
        @Autowired val sessionRepo: ServerSessionRepository,
        @Autowired val sessionEventRepo: ServerSessionEventRepository) {

    private val testId = "TestUserId"
    private val group1 = "group1"
    private val group2 = "group2"
    private val additional = setOf(group1, group2)
    private val savedSession = slot<ServerSessionEntity>()
    private val savedEvent = slot<ServerSessionEventEntity>()

    @BeforeEach
    fun setup() {
        clearAllMocks()
        every { sessionRepo.save(capture(savedSession)) } answers{ savedSession.captured }
        every { sessionEventRepo.save(capture(savedEvent)) } answers{ savedEvent.captured }
    }

    @Test
    fun `Create session from username login`() {
        val usernameLogin = sessionService.createSession(SessionPrompt(testId, additional, AuthenticationType.USERNAME))
        verify(exactly = 1) { sessionRepo.save(ofType(ServerSessionEntity::class)) }
        verify(exactly = 1) { sessionEventRepo.save(ofType(ServerSessionEventEntity::class)) }
        Assertions.assertEquals(SessionEvent.LOGIN.name, savedEvent.captured.event)
        Assertions.assertEquals(36, usernameLogin.sessionId.length)
        Assertions.assertEquals(testId, usernameLogin.userId)
        Assertions.assertEquals(AuthenticationType.USERNAME, usernameLogin.type)
        Assertions.assertNull(usernameLogin.description)
        Assertions.assertNotNull(usernameLogin.token)
        Assertions.assertTrue(usernameLogin.end.getTimestamp().after(Timestamp.from(Instant.now())))
        val grant = usernameLogin.token?.let { TokenDecoder.decodeToken(it) }
        Assertions.assertNotNull(grant)
    }

    @Test
    fun `Create session from email login`() {
        val emailLogin = sessionService.createSession(SessionPrompt(testId, additional, AuthenticationType.EMAIL))
        verify(exactly = 1) { sessionRepo.save(ofType(ServerSessionEntity::class)) }
        verify(exactly = 1) { sessionEventRepo.save(ofType(ServerSessionEventEntity::class)) }
        val grant = emailLogin.token?.let { TokenDecoder.decodeToken(it) }
        Assertions.assertNotNull(grant)
    }

    @Test
    fun `Create session without a user ID`() {
        assertThrows<NoAuthException> { sessionService.createSession(SessionPrompt("", additional, AuthenticationType.USERNAME)) }
        verify(exactly = 0) { sessionRepo.save(ofType(ServerSessionEntity::class)) }
        verify(exactly = 0) { sessionEventRepo.save(ofType(ServerSessionEventEntity::class)) }
    }

    @Test
    fun `Verify an existing session`() {
        val usernameLogin = sessionService.createSession(SessionPrompt(testId, additional, AuthenticationType.USERNAME))
        every { sessionRepo.findByIdOrNull(usernameLogin.sessionId) } answers { savedSession.captured }
        val verified = usernameLogin.token?.let { sessionService.verifySession(it) }
        Assertions.assertNotNull(verified)
        Assertions.assertEquals(usernameLogin.token, verified?.token)
    }

    @Test
    fun `Verify an expired session`() {
        val usernameLogin = sessionService.createSession(SessionPrompt(testId, additional, AuthenticationType.USERNAME))
        savedSession.captured.endDateTime = Timestamp.from(Instant.now().minusSeconds(20L))
        every { sessionRepo.findByIdOrNull(usernameLogin.sessionId) } answers { savedSession.captured }
        assertThrows<NoAuthException> { usernameLogin.token?.let { sessionService.verifySession(it) } }
    }

    @Test
    fun `Verify a non-existing session`() {
        val usernameLogin = sessionService.createSession(SessionPrompt(testId, additional, AuthenticationType.USERNAME))
        every { sessionRepo.findByIdOrNull(usernameLogin.sessionId) } answers { null }
        assertThrows<NoAuthException> { usernameLogin.token?.let { sessionService.verifySession(it) } }
    }

    @Test
    fun `Verify a fake token`() {
        assertThrows<NoAuthException> { sessionService.verifySession("AFakeToken") }
    }

    @Test
    fun `Refresh an existing session`() {
        val usernameLogin = sessionService.createSession(SessionPrompt(testId, additional, AuthenticationType.USERNAME))
        every { sessionRepo.findByIdOrNull(usernameLogin.sessionId) } answers { savedSession.captured }
        Thread.sleep(1000L)
        val refreshed = usernameLogin.token?.let { sessionService.refreshSession(it) }
        Assertions.assertEquals(SessionEvent.REFRESH.name, savedEvent.captured.event)
        refreshed?.end?.isAfter(usernameLogin.end)?.let { Assertions.assertTrue(it) }
        Assertions.assertNotEquals(usernameLogin.token, refreshed?.token)
    }

    @Test
    fun `Refresh an expired session`() {
        val usernameLogin = sessionService.createSession(SessionPrompt(testId, additional, AuthenticationType.USERNAME))
        savedSession.captured.endDateTime = Timestamp.from(Instant.now().minusSeconds(20L))
        every { sessionRepo.findByIdOrNull(usernameLogin.sessionId) } answers { savedSession.captured }
        assertThrows<NoAuthException> { usernameLogin.token?.let { sessionService.refreshSession(it) } }
    }

    @Test
    fun `Refresh a fake token`() {
        assertThrows<NoAuthException> { sessionService.refreshSession("AFakeToken") }
    }

    @Test
    fun `Expire a valid session`() {
        val usernameLogin = sessionService.createSession(SessionPrompt(testId, additional, AuthenticationType.USERNAME))
        every { sessionRepo.findByIdOrNull(usernameLogin.sessionId) } answers { savedSession.captured }
        val expired = usernameLogin.token?.let { sessionService.expireSession(it) }
        Assertions.assertNotNull(expired)
        Assertions.assertNotNull(expired?.token)
    }

    @Test
    fun `Expire an expired session`() {
        val usernameLogin = sessionService.createSession(SessionPrompt(testId, additional, AuthenticationType.USERNAME))
        savedSession.captured.endDateTime = Timestamp.from(Instant.now().minusSeconds(20L))
        every { sessionRepo.findByIdOrNull(usernameLogin.sessionId) } answers { savedSession.captured }
        assertThrows<NoAuthException> { usernameLogin.token?.let { sessionService.expireSession(it) } }
    }

    @Test
    fun `Expire an invalid token`() {
        assertThrows<NoAuthException> { sessionService.expireSession("AFakeToken") }
    }

    @Test
    fun `Update a user's session`() {
        sessionService.createSession(SessionPrompt(testId, additional, AuthenticationType.USERNAME))
        every { sessionRepo.findAllByUserIdAndEndDateTimeIsAfter(testId, any()) } answers { setOf(savedSession.captured) }
        sessionService.updateUserSessions(testId, Collections.emptySet())
        Assertions.assertEquals(SessionEvent.GROUPS_CHANGE.name, savedEvent.captured.event)
        Assertions.assertEquals("", savedSession.captured.additional)
    }

    @Test
    fun `Update a non-existing user's session`() {
        every { sessionRepo.findAllByUserIdAndEndDateTimeIsAfter(testId, any()) } answers { Collections.emptySet() }
        sessionService.updateUserSessions(testId, additional)
        verify(exactly = 0) { sessionRepo.save(ofType(ServerSessionEntity::class)) }
        verify(exactly = 0) { sessionEventRepo.save(ofType(ServerSessionEventEntity::class)) }
    }

}
