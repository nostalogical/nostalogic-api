package net.nostalogic.access.controllers

import net.nostalogic.access.AccessApplication
import net.nostalogic.access.persistence.repositories.ServerSessionEventRepository
import net.nostalogic.access.persistence.repositories.ServerSessionRepository
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.NoStrings
import net.nostalogic.datamodel.ErrorResponse
import net.nostalogic.security.grants.LoginGrant
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.security.models.SessionSummary
import net.nostalogic.security.utils.TokenDecoder
import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.util.*

@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [AccessApplication::class])
class SessionControllerTest(
        @Autowired val dbLoader: DatabaseLoader
) {

    private val localhost = "http://localhost:"

    @Value("\${local.server.port}")
    private var port: Int? = null
    private var baseApiUrl = localhost

    private val userId = "TestUserId"
    private val group1 = "group1"
    private val group2 = "group2"
    private val additional = setOf(group1, group2)

    @BeforeEach
    fun setup() {
        baseApiUrl = localhost + port
        dbLoader.runDbCleanSetup()
    }

    @AfterEach
    fun teardown() {
        dbLoader.runDataWipeScripts()
        dbLoader.runSchemaDropScripts()
    }

    private fun sessionsUrl(): String {
        return baseApiUrl + SessionController.SESSIONS_ENDPOINT
    }

    private fun createTemplate(): RestTemplate {
        val template = RestTemplate()
        template.requestFactory = HttpComponentsClientHttpRequestFactory()
        template.errorHandler = object : DefaultResponseErrorHandler() {
            @Throws(IOException::class)
            override fun hasError(response: ClientHttpResponse): Boolean {
                val status = response.statusCode
                return status.series() == HttpStatus.Series.SERVER_ERROR
            }
        }
        return template
    }

    private fun <T> exchange(entity: HttpEntity<*>, responseType: ParameterizedTypeReference<T>, method: HttpMethod, url: String): ResponseEntity<T> {
        return createTemplate().exchange(url, method, entity, responseType)
    }

    private fun createSession(entity: HttpEntity<*>, method: HttpMethod): ResponseEntity<SessionSummary> {
        return exchange(
                entity = entity,
                responseType = object : ParameterizedTypeReference<SessionSummary>() {},
                method = method,
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
        Assertions.assertEquals(401, verification.statusCodeValue)
        return verification.body!!
    }

    @Test
    fun `Create session from username login`() {
        val result = createSession(HttpEntity(SessionPrompt(userId, additional, AuthenticationType.USERNAME)), HttpMethod.POST)
        Assertions.assertNotNull(result)
        Assertions.assertEquals(200, result.statusCodeValue)
        val summary = result.body as SessionSummary
        Assertions.assertTrue(summary.sessionId.length == 36)
        Assertions.assertEquals(userId, summary.userId)
        Assertions.assertEquals(AuthenticationType.USERNAME, summary.type)
        Assertions.assertTrue(summary.end.isAfter(summary.start))
        Assertions.assertTrue(StringUtils.isNoneBlank(summary.token))
        val grant = summary.token?.let { TokenDecoder.decodeToken(it) }
        Assertions.assertNotNull(grant)
        Assertions.assertEquals(summary.userId, grant?.subject)
    }

    @Test
    fun `Create session from email login`() {
        val result = createSession(HttpEntity(SessionPrompt(userId, additional, AuthenticationType.EMAIL)), HttpMethod.POST)
        Assertions.assertNotNull(result)
        Assertions.assertEquals(200, result.statusCodeValue)
        Assertions.assertEquals(AuthenticationType.EMAIL, (result.body as SessionSummary).type)
    }

    @Test
    fun `Create session without a user ID`() {
        val result = exchange(
                entity = HttpEntity(SessionPrompt("", additional, AuthenticationType.EMAIL)),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.POST,
                url = sessionsUrl()
        )
        Assertions.assertNotNull(result)
        Assertions.assertEquals(401, result.statusCodeValue)
        Assertions.assertEquals(201007, (result.body as ErrorResponse).errorCode)
    }

    @Test
    fun `Verify an existing session`() {
        val result = createSession(HttpEntity(SessionPrompt(userId, additional, AuthenticationType.USERNAME)), HttpMethod.POST)
        val token = result.body!!.token!!
        val authResult = authenticateSession(token)
        Assertions.assertEquals(200, authResult.statusCodeValue)
        val summary = result.body as SessionSummary
        Assertions.assertEquals(userId, summary.userId)
    }

    @Test
    fun `Verify a non-existing session`() {
        val result = createSession(HttpEntity(SessionPrompt(userId, additional, AuthenticationType.USERNAME)), HttpMethod.POST)
        val summary = result.body as SessionSummary
        val token = summary.token!!
        dbLoader.runDataWipeScripts()
        val loginFail = expectFailure(token, HttpMethod.GET)
        Assertions.assertEquals(201003, loginFail.errorCode)
    }

    @Test
    fun `Verify a fake token`() {
        val loginFail = expectFailure("xcvb", HttpMethod.GET)
        Assertions.assertEquals(102001, loginFail.errorCode)
    }

    @Test
    fun `Refresh an existing session`() {
        val result = createSession(HttpEntity(SessionPrompt(userId, additional, AuthenticationType.USERNAME)), HttpMethod.POST)
        val refresh = exchange(
                entity = tokenHeader(result.body!!.token!!),
                responseType = object : ParameterizedTypeReference<SessionSummary>() {},
                method = HttpMethod.PUT,
                url = sessionsUrl()
        )
        Assertions.assertEquals(200, refresh.statusCodeValue)
        Assertions.assertEquals(userId, refresh.body!!.userId)
    }

    @Test
    fun `Refresh an expired session`() {
        val result = createSession(HttpEntity(SessionPrompt(userId, additional, AuthenticationType.USERNAME)), HttpMethod.POST)
        val token = result.body!!.token!!
        expireSession(token)
        val refreshFailure = expectFailure(token, HttpMethod.PUT)
        Assertions.assertEquals(201005, refreshFailure.errorCode)
    }

    @Test
    fun `Refresh a fake token`() {
        val refreshFailure = expectFailure("asfsdf", HttpMethod.PUT)
        Assertions.assertEquals(102001, refreshFailure.errorCode)
    }

    @Test
    fun `Expire a valid session`() {
        val result = createSession(HttpEntity(SessionPrompt(userId, additional, AuthenticationType.USERNAME)), HttpMethod.POST)
        val token = result.body!!.token!!
        expireSession(token)
        val loginFail = expectFailure(token, HttpMethod.GET)
        Assertions.assertEquals(201004, loginFail.errorCode)
    }

    @Test
    fun `Expire an expired session`() {
        val result = createSession(HttpEntity(SessionPrompt(userId, additional, AuthenticationType.USERNAME)), HttpMethod.POST)
        val token = result.body!!.token!!
        expireSession(token)
        val secondExpire = expectFailure(token, HttpMethod.DELETE)
        Assertions.assertEquals(201006, secondExpire.errorCode)
    }

    @Test
    fun `Expire an invalid token`() {
        val secondExpire = expectFailure("invalid", HttpMethod.DELETE)
        Assertions.assertEquals(102001, secondExpire.errorCode)
    }

    @Test
    fun `Update a user's session`() {
        val newGroups = setOf("new_one", "new_two", "new_three")
        val usernameSession = createSession(HttpEntity(SessionPrompt(userId, additional, AuthenticationType.USERNAME)), HttpMethod.POST)
        val emailSession = createSession(HttpEntity(SessionPrompt(userId, Collections.emptySet(), AuthenticationType.EMAIL)), HttpMethod.POST)
        exchange(entity = HttpEntity(newGroups),
                responseType = object : ParameterizedTypeReference<Any>() {},
                method = HttpMethod.PUT,
                url = sessionsUrl() + "/update/" + userId)
        val usernameAuth = authenticateSession(usernameSession.body!!.token!!)
        val emailAuth = authenticateSession(emailSession.body!!.token!!)
        val usernameGrant = TokenDecoder.decodeToken(usernameAuth.body!!.token!!) as LoginGrant
        val emailGrant = TokenDecoder.decodeToken(emailAuth.body!!.token!!) as LoginGrant
        Assertions.assertEquals(newGroups, usernameGrant.additional)
        Assertions.assertEquals(newGroups, emailGrant.additional)
    }

    @Test
    fun `Update a non-existing user's session`() {
        val update = exchange(
                entity = HttpEntity(setOf("new_one", "new_two")),
                responseType = object : ParameterizedTypeReference<Any>() {},
                method = HttpMethod.PUT,
                url = sessionsUrl() + "/update/fakeuserid"
        )
        Assertions.assertEquals(200, update.statusCodeValue,
                "This endpoint should always return 200, as a user with no any active sessions is " +
                        "functionally the same as a nonexistent user.")
    }

}
