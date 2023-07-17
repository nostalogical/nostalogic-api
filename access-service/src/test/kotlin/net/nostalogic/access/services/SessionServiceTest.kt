package net.nostalogic.access.services

import io.mockk.*
import net.nostalogic.access.config.AccessTestConfig
import net.nostalogic.access.persistence.entities.AccessExtensionEntity
import net.nostalogic.access.persistence.entities.ServerSessionEntity
import net.nostalogic.access.persistence.entities.ServerSessionEventEntity
import net.nostalogic.access.persistence.repositories.AccessExtensionRepository
import net.nostalogic.access.persistence.repositories.ServerSessionEventRepository
import net.nostalogic.access.persistence.repositories.ServerSessionRepository
import net.nostalogic.constants.*
import net.nostalogic.datamodel.NoDate
import net.nostalogic.exceptions.NoAuthException
import net.nostalogic.security.models.GroupsAccessUpdate
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.security.utils.TokenDecoder
import org.junit.jupiter.api.Assertions.*
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
import kotlin.collections.HashSet

@ActiveProfiles(profiles = ["test"])
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AccessTestConfig::class])
class SessionServiceTest(
        @Autowired val sessionService: SessionService,
        @Autowired val sessionRepo: ServerSessionRepository,
        @Autowired val sessionEventRepo: ServerSessionEventRepository,
        @Autowired val accessExtensionRepo: AccessExtensionRepository,
    ) {

    private val testId = "TestUserId"
    private val group1 = "group1"
    private val group2 = "group2"
    private val additional = setOf(group1, group2)
    private val savedSession = slot<ServerSessionEntity>()
    private val savedEvent = slot<ServerSessionEventEntity>()
    private val savedAccessExtensions = slot<Iterable<AccessExtensionEntity>>()

    @BeforeEach
    fun setup() {
        clearAllMocks()
        every { sessionRepo.save(capture(savedSession)) } answers{ savedSession.captured }
        every { sessionEventRepo.save(capture(savedEvent)) } answers{ savedEvent.captured }
        every { accessExtensionRepo.saveAll(capture(savedAccessExtensions)) } answers{ savedAccessExtensions.captured }
    }

    @Test
    fun `Create session from username login`() {
        val usernameLogin = sessionService.createSession(sessionPrompt())
        verify(exactly = 1) { sessionRepo.save(ofType(ServerSessionEntity::class)) }
        verify(exactly = 1) { sessionEventRepo.save(ofType(ServerSessionEventEntity::class)) }
        assertEquals(SessionEvent.BEGIN.name, savedEvent.captured.sessionEvent.name)
        assertEquals(36, usernameLogin.sessionId.length)
        assertEquals(testId, usernameLogin.userId)
        assertEquals(AuthenticationType.LOGIN, usernameLogin.type)
        assertNull(usernameLogin.description)
        assertNotNull(usernameLogin.accessToken)
        assertTrue(usernameLogin.end.getTimestamp().after(Timestamp.from(Instant.now())))
        val grant = usernameLogin.accessToken?.let { TokenDecoder.decodeToken(it.token) }
        assertNotNull(grant)
    }

    @Test
    fun `Create session from email login`() {
        val emailLogin = sessionService.createSession(sessionPrompt(source = AuthenticationSource.EMAIL))
        verify(exactly = 1) { sessionRepo.save(ofType(ServerSessionEntity::class)) }
        verify(exactly = 1) { sessionEventRepo.save(ofType(ServerSessionEventEntity::class)) }
        val grant = emailLogin.accessToken?.let { TokenDecoder.decodeToken(it.token) }
        assertNotNull(grant)
    }

    @Test
    fun `Create session without a user ID`() {
        assertThrows<NoAuthException> { sessionService.createSession(sessionPrompt(id = "")) }
        verify(exactly = 0) { sessionRepo.save(ofType(ServerSessionEntity::class)) }
        verify(exactly = 0) { sessionEventRepo.save(ofType(ServerSessionEventEntity::class)) }
    }

    @Test
    fun `Create an impersonation session`() {
        val impersonation = sessionService.createSession(SessionPrompt(testId, AuthenticationSource.IMPERSONATION,
            originalUserId = "OriginalId", tenant = Tenant.NOSTALOGIC.name))
        verify(exactly = 1) { sessionRepo.save(ofType(ServerSessionEntity::class)) }
        verify(exactly = 1) { sessionEventRepo.save(ofType(ServerSessionEventEntity::class)) }
        val grant = impersonation.accessToken?.let { TokenDecoder.decodeToken(it.token) }
        assertNotNull(grant)
        assertNotNull(impersonation.accessToken)
        assertNull(impersonation.refreshToken, "Impersonation sessions should not be refreshable.")
    }

    @Test
    fun `An impersonation session requires an original user`() {
        val exception = assertThrows<NoAuthException> { sessionService.createSession(sessionPrompt(source = AuthenticationSource.IMPERSONATION)) }
        assertEquals(ExceptionCodes._0201011, exception.errorCode)
    }

    @Test
    fun `Verify an existing session`() {
        val usernameLogin = sessionService.createSession(sessionPrompt())
        assertNotNull(usernameLogin.accessToken)
        every { sessionRepo.findByIdOrNull(usernameLogin.sessionId) } answers { savedSession.captured }
        val verified = usernameLogin.accessToken?.let { sessionService.verifySession(it.token) }
        assertNotNull(verified)
        assertEquals(usernameLogin.sessionId, verified?.sessionId)
        assertNull(verified!!.accessToken)
        assertNull(verified.refreshToken)
    }

    @Test
    fun `Verify an expired session`() {
        val usernameLogin = sessionService.createSession(sessionPrompt())
        savedSession.captured.endDateTime = Timestamp.from(Instant.now().minusSeconds(20L))
        every { sessionRepo.findByIdOrNull(usernameLogin.sessionId) } answers { savedSession.captured }
        assertThrows<NoAuthException> { usernameLogin.accessToken?.let { sessionService.verifySession(it.token) } }
    }

    @Test
    fun `Verify a non-existing session`() {
        val usernameLogin = sessionService.createSession(sessionPrompt())
        every { sessionRepo.findByIdOrNull(usernameLogin.sessionId) } answers { null }
        assertThrows<NoAuthException> { usernameLogin.accessToken?.let { sessionService.verifySession(it.token) } }
    }

    @Test
    fun `Verify a fake token`() {
        assertThrows<NoAuthException> { sessionService.verifySession("AFakeToken") }
    }

    @Test
    fun `Refresh an existing session`() {
        val usernameLogin = sessionService.createSession(sessionPrompt())
        every { sessionRepo.findByIdOrNull(usernameLogin.sessionId) } answers { savedSession.captured }
        Thread.sleep(10L)
        val refreshed = usernameLogin.refreshToken?.let { sessionService.refreshSession(it.token) }
        assertEquals(SessionEvent.REFRESH.name, savedEvent.captured.sessionEvent.name)
        refreshed?.end?.isAfter(usernameLogin.end)?.let { assertTrue(it) }
        assertNotEquals(usernameLogin.accessToken, refreshed?.accessToken)
    }

    @Test
    fun `Refresh an expired session`() {
        val usernameLogin = sessionService.createSession(sessionPrompt())
        savedSession.captured.endDateTime = Timestamp.from(Instant.now().minusSeconds(20L))
        every { sessionRepo.findByIdOrNull(usernameLogin.sessionId) } answers { savedSession.captured }
        assertThrows<NoAuthException> { usernameLogin.accessToken?.let { sessionService.refreshSession(it.token) } }
    }

    @Test
    fun `Refresh a fake token`() {
        assertThrows<NoAuthException> { sessionService.refreshSession("AFakeToken") }
    }

    @Test
    fun `Expire a valid session`() {
        val usernameLogin = sessionService.createSession(sessionPrompt())
        every { sessionRepo.findByIdOrNull(usernameLogin.sessionId) } answers { savedSession.captured }
        val expired = usernameLogin.accessToken?.let { sessionService.expireSession(it.token) }
        assertNotNull(expired)
        Thread.sleep(10L)
        assertTrue(expired!!.end.isBefore(NoDate()))
        assertNull(expired.accessToken)
    }

    @Test
    fun `Expire an expired session`() {
        val usernameLogin = sessionService.createSession(sessionPrompt())
        savedSession.captured.endDateTime = Timestamp.from(Instant.now().minusSeconds(20L))
        every { sessionRepo.findByIdOrNull(usernameLogin.sessionId) } answers { savedSession.captured }
        assertThrows<NoAuthException> { usernameLogin.accessToken?.let { sessionService.expireSession(it.token) } }
    }

    @Test
    fun `Expire an invalid token`() {
        assertThrows<NoAuthException> { sessionService.expireSession("AFakeToken") }
    }

    @Test
    fun `Update a user's session`() {
        sessionService.createSession(sessionPrompt())
        every { sessionRepo.findAllByUserIdAndEndDateTimeIsAfter(testId, any()) } answers { hashSetOf(savedSession.captured) }
        every { accessExtensionRepo.findAllByUserId(testId) } answers { emptySet() }
        every { accessExtensionRepo.deleteAllByUserId(testId) } answers { }
        sessionService.updateUserGroups(testId, groupUpdate(hashSetOf(group1, group2)))

        verify(exactly = 1) { accessExtensionRepo.deleteAllByUserId(testId) }
        assertEquals(2, savedAccessExtensions.captured.count())
        savedAccessExtensions.captured.forEach { assertEquals(testId, it.userId) }
        val groupIds = savedAccessExtensions.captured.map { it.entityId }.toHashSet()
        assertTrue(groupIds.contains(group1))
        assertTrue(groupIds.contains(group2))

        assertEquals(SessionEvent.GROUPS_CHANGE.name, savedEvent.captured.sessionEvent.name)
    }

    @Test
    fun `Update a user's session with no group changes`() {
        sessionService.createSession(sessionPrompt())
        every { sessionRepo.findAllByUserIdAndEndDateTimeIsAfter(testId, any()) } answers { hashSetOf(savedSession.captured) }
        val existingGroups = hashSetOf(AccessExtensionEntity(testId, group1, tenant = Tenant.NOSTALOGIC), AccessExtensionEntity(testId, group2, tenant = Tenant.NOSTALOGIC))
        every { accessExtensionRepo.findAllByUserId(testId) } answers { existingGroups }
        every { accessExtensionRepo.deleteAllByUserId(testId) } answers { }
        sessionService.updateUserGroups(testId, groupUpdate(hashSetOf(group1, group2)))

        verify(exactly = 1) { accessExtensionRepo.deleteAllByUserId(testId) }
        assertEquals(2, savedAccessExtensions.captured.count())
        savedAccessExtensions.captured.forEach { assertEquals(testId, it.userId) }
        val groupIds = savedAccessExtensions.captured.map { it.entityId }.toHashSet()
        assertTrue(groupIds.contains(group1))
        assertTrue(groupIds.contains(group2))

        assertEquals(SessionEvent.BEGIN.name, savedEvent.captured.sessionEvent.name,
            "A group change session event shouldn't be recorded if no actual change was made.")
    }

    @Test
    fun `Update a non-existing user's session`() {
        every { sessionRepo.findAllByUserIdAndEndDateTimeIsAfter(testId, any()) } answers { HashSet() }
        every { accessExtensionRepo.findAllByUserId(testId) } answers { HashSet() }
        every { accessExtensionRepo.deleteAllByUserId(testId) } answers { }
        every { accessExtensionRepo.saveAll(any<List<AccessExtensionEntity>>()) } answers { mockk() }
        sessionService.updateUserGroups(testId, groupUpdate(additional))
        verify(exactly = 0) { sessionRepo.save(ofType(ServerSessionEntity::class)) }
        verify(exactly = 0) { sessionEventRepo.save(ofType(ServerSessionEventEntity::class)) }
    }

    private fun sessionPrompt(
        id: String = testId,
        source: AuthenticationSource = AuthenticationSource.USERNAME,
        tenant: Tenant = Tenant.NOSTALOGIC): SessionPrompt {
        return SessionPrompt(id, source, tenant = tenant.name)
    }

    private fun groupUpdate(groups: Set<String>): GroupsAccessUpdate {
        return GroupsAccessUpdate(updaterId = testId, tenant = Tenant.NOSTALOGIC.name, groups = groups)
    }
}
