package net.nostalogic.access.controllers

import net.nostalogic.access.AccessApplication
import net.nostalogic.access.config.TestPostgresContainer
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.constants.AuthenticationSource
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.ExceptionCodes._0201003
import net.nostalogic.constants.ExceptionCodes._0201004
import net.nostalogic.constants.ExceptionCodes._0201005
import net.nostalogic.constants.ExceptionCodes._0201006
import net.nostalogic.constants.ExceptionCodes._0201007
import net.nostalogic.constants.NoStrings
import net.nostalogic.datamodel.ErrorResponse
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.security.models.SessionSummary
import net.nostalogic.security.utils.TokenDecoder
import net.nostalogic.utils.EntityUtils
import org.apache.commons.lang3.StringUtils
import org.junit.ClassRule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*

@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [AccessApplication::class])
class SessionControllerTest(@Autowired dbLoader: DatabaseLoader): BaseControllerTest(dbLoader) {

    companion object {
        @JvmField
        @ClassRule
        var postgreSQLContainer: PostgreSQLContainer<*> = TestPostgresContainer.getInstance("test_nostalogic_access")

        @JvmStatic
        @BeforeAll
        fun beforeAll(): Unit {
            postgreSQLContainer.start()
        }
    }

    private val userId = EntityUtils.uuid()

    private fun sessionsUrl(): String {
        return baseApiUrl + SessionController.SESSIONS_ENDPOINT
    }

    private fun createSession(entity: HttpEntity<*>): ResponseEntity<SessionSummary> {
        return exchange(
                entity = entity,
                responseType = object : ParameterizedTypeReference<SessionSummary>() {},
                method = HttpMethod.POST,
                url = sessionsUrl()
        )
    }

    private fun tokenHeader(token: String): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.set(NoStrings.AUTH_HEADER, token)
        return HttpEntity(String(), headers)
    }

    private fun authenticateSession(token: String): ResponseEntity<SessionSummary> {
        return exchange(
                entity = tokenHeader(token),
                responseType = object : ParameterizedTypeReference<SessionSummary>() {},
                method = HttpMethod.GET,
                url = sessionsUrl()
        )
    }

    private fun expireSession(token: String): ResponseEntity<SessionSummary> {
        return exchange(
                entity = tokenHeader(token),
                responseType = object : ParameterizedTypeReference<SessionSummary>() {},
                method = HttpMethod.DELETE,
                url = sessionsUrl()
        )
    }

    private fun expectFailure(token: String, method: HttpMethod): ErrorResponse {
        val verification = exchange(
                entity = tokenHeader(token),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = method,
                url = sessionsUrl()
        )
        assertEquals(401, verification.statusCodeValue)
        return verification.body!!
    }

    @Test
    fun `Create session from username login`() {
        val result = createSession(HttpEntity(SessionPrompt(userId, AuthenticationSource.USERNAME)))
        assertNotNull(result)
        assertEquals(200, result.statusCodeValue)
        val summary = result.body as SessionSummary
        assertTrue(summary.sessionId.length == 36)
        assertEquals(userId, summary.userId)
        assertEquals(AuthenticationType.LOGIN, summary.type)
        assertTrue(summary.end.isAfter(summary.start))
        assertTrue(StringUtils.isNoneBlank(summary.accessToken?.token))
        val grant = summary.accessToken?.let { TokenDecoder.decodeToken(it.token) }
        assertNotNull(grant)
        assertEquals(summary.userId, grant?.subject)
    }

    @Test
    fun `Create session from email login`() {
        val result = createSession(HttpEntity(SessionPrompt(userId, AuthenticationSource.EMAIL)))
        assertNotNull(result)
        assertEquals(200, result.statusCodeValue)
        assertEquals(AuthenticationType.LOGIN, (result.body as SessionSummary).type)
    }

    @Test
    fun `Create session without a user ID`() {
        val result = exchange(
                entity = HttpEntity(SessionPrompt("", AuthenticationSource.EMAIL)),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.POST,
                url = sessionsUrl()
        )
        assertNotNull(result)
        assertEquals(401, result.statusCodeValue)
        assertEquals(_0201007, (result.body as ErrorResponse).errorCode)
    }

    @Test
    fun `Verify an existing session`() {
        val result = createSession(HttpEntity(SessionPrompt(userId, AuthenticationSource.USERNAME)))
        val token = result.body!!.accessToken!!.token
        val authResult = authenticateSession(token)
        assertEquals(200, authResult.statusCodeValue)
        val summary = result.body as SessionSummary
        assertEquals(userId, summary.userId)
    }

    @Test
    fun `Verify a non-existing session`() {
        val result = createSession(HttpEntity(SessionPrompt(userId, AuthenticationSource.USERNAME)))
        val summary = result.body as SessionSummary
        val token = summary.accessToken!!.token
        dbLoader.runDataWipeScripts()
        val loginFail = expectFailure(token, HttpMethod.GET)
        assertEquals(_0201003, loginFail.errorCode)
    }

    @Test
    fun `Verify a fake token`() {
        val loginFail = expectFailure("xcvb", HttpMethod.GET)
        assertEquals(102001, loginFail.errorCode)
    }

    @Test
    fun `Refresh an existing session`() {
        val result = createSession(HttpEntity(SessionPrompt(userId, AuthenticationSource.USERNAME)))
        val refresh = exchange(
                entity = tokenHeader(result.body!!.refreshToken!!.token),
                responseType = object : ParameterizedTypeReference<SessionSummary>() {},
                method = HttpMethod.PUT,
                url = sessionsUrl()
        )
        assertEquals(200, refresh.statusCodeValue)
        assertEquals(userId, refresh.body!!.userId)
    }

    @Test
    fun `Refresh an expired session`() {
        val result = createSession(HttpEntity(SessionPrompt(userId, AuthenticationSource.USERNAME)))
        val accessToken = result.body!!.accessToken!!.token
        val refreshToken = result.body!!.refreshToken!!.token
        expireSession(accessToken)
        val refreshFailure = expectFailure(refreshToken, HttpMethod.PUT)
        assertEquals(_0201005, refreshFailure.errorCode)
    }

    @Test
    fun `Refresh a fake token`() {
        val refreshFailure = expectFailure("asfsdf", HttpMethod.PUT)
        assertEquals(102001, refreshFailure.errorCode)
    }

    @Test
    fun `Expire a valid session`() {
        val result = createSession(HttpEntity(SessionPrompt(userId, AuthenticationSource.USERNAME)))
        val token = result.body!!.accessToken!!.token
        expireSession(token)
        val loginFail = expectFailure(token, HttpMethod.GET)
        assertEquals(_0201004, loginFail.errorCode)
    }

    @Test
    fun `Expire an expired session`() {
        val result = createSession(HttpEntity(SessionPrompt(userId, AuthenticationSource.USERNAME)))
        val token = result.body!!.accessToken!!.token
        expireSession(token)
        val secondExpire = expectFailure(token, HttpMethod.DELETE)
        assertEquals(_0201006, secondExpire.errorCode)
    }

    @Test
    fun `Expire an invalid token`() {
        val secondExpire = expectFailure("invalid", HttpMethod.DELETE)
        assertEquals(102001, secondExpire.errorCode)
    }

    @Test
    fun `Update a user's session`() {
        val newGroups = setOf("new_one", "new_two", "new_three")
        val usernameSession = createSession(HttpEntity(SessionPrompt(userId, AuthenticationSource.USERNAME)))
        val emailSession = createSession(HttpEntity(SessionPrompt(userId, AuthenticationSource.EMAIL)))
        exchange(entity = HttpEntity(newGroups),
                responseType = object : ParameterizedTypeReference<Any>() {},
                method = HttpMethod.PUT,
                url = sessionsUrl() + "/update/" + userId)
        val usernameAuth = authenticateSession(usernameSession.body!!.accessToken!!.token)
        val emailAuth = authenticateSession(emailSession.body!!.accessToken!!.token)
        assertNotNull(usernameAuth.body)
        assertNull(usernameAuth.body!!.accessToken, "A token should not be supplied for an authentication check")
        assertNotNull(emailAuth.body)
        assertNull(emailAuth.body!!.accessToken, "A token should not be supplied for an authentication check")
    }

    @Test
    fun `Update a non-existing user's session`() {
        val update = exchange(
                entity = HttpEntity(setOf("new_one", "new_two")),
                responseType = object : ParameterizedTypeReference<Any>() {},
                method = HttpMethod.PUT,
                url = sessionsUrl() + "/update/fakeuserid"
        )
        assertEquals(200, update.statusCodeValue,
                "This endpoint should always return 200, as a user with no any active sessions is " +
                        "functionally the same as a nonexistent user.")
    }

}
