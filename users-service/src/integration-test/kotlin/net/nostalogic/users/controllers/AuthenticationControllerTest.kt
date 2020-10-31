package net.nostalogic.users.controllers

import io.mockk.every
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.NoStrings
import net.nostalogic.datamodel.NoDate
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.AccessReport
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.NoEntity
import net.nostalogic.security.grants.LoginGrant
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.security.models.SessionSummary
import net.nostalogic.security.utils.TokenDecoder
import net.nostalogic.security.utils.TokenEncoder
import net.nostalogic.users.UsersApplication
import net.nostalogic.users.datamodel.authentication.AuthenticationResponse
import net.nostalogic.users.datamodel.authentication.ImpersonationRequest
import net.nostalogic.users.datamodel.authentication.LoginRequest
import net.nostalogic.utils.EntityUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.temporal.ChronoUnit

@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [UsersApplication::class])
class AuthenticationControllerTest(@Autowired dbLoader: DatabaseLoader): BaseControllerTest(dbLoader) {

    private val OWNER_NAME = "Test Site Owner"
    private val OWNER_ID = "c1e99394-08e6-4a65-9a66-b33f64b3ba74"
    private val OWNER_PASSWORD = "TestSiteOwner"
    private val REGULAR_NAME = "Generic User"
    private val REGULAR_ID = "a9b0dea5-0515-4f77-b358-45e3fd6fc340"
    private val REGULAR_PASSWORD = "GenericUser"

    private val endDate = NoDate.plus(7, ChronoUnit.DAYS)

    private fun doLogin(userId: String = OWNER_ID, username: String = OWNER_NAME, password: String = OWNER_PASSWORD): AuthenticationResponse {
        val grant = LoginGrant(userId, emptySet(), endDate, EntityUtils.uuid(), AuthenticationType.USERNAME)
        val token = TokenEncoder.encodeLoginGrant(grant)
        every { accessComms.createSession(ofType(SessionPrompt::class)) } answers {
            SessionSummary(EntityUtils.uuid(), userId, grant.type, NoDate(), endDate, null, token) }
        return exchange(
                entity = HttpEntity(LoginRequest(username, password, null)),
                responseType = object : ParameterizedTypeReference<AuthenticationResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${AuthenticationController.AUTH_ENDPOINT}${AuthenticationController.LOGIN_URI}").body!!
    }

    @Test
    fun `Normal login`() {
        val response = doLogin()
        Assertions.assertNotNull(response)
        Assertions.assertTrue(response.authenticated)
        Assertions.assertNotNull(response.token)
        Assertions.assertEquals(endDate, response.expiration)
    }

    @Test
    fun `Site Owner can impersonate a regular user`() {
        val session = doLogin()
        val headers = HttpHeaders()
        headers.set(NoStrings.AUTH_HEADER, session.token)
        every { accessComms.query(ofType(AccessQuery::class)) } answers {
            AccessReport(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, true))))) }
        val exchange = exchange(
                entity = HttpEntity(ImpersonationRequest(REGULAR_ID), headers),
                responseType = object : ParameterizedTypeReference<AuthenticationResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${AuthenticationController.AUTH_ENDPOINT}${AuthenticationController.IMPERSONATE_URI}").body!!
        Assertions.assertTrue(exchange.authenticated)
        TokenDecoder.decodeToken("ZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKSVV6VXhNaUo5LmV5SnpaWE1pT2lKaE4yTmhPR014WXkxbE9UVXhMVFJoWkdRdFlUUXdaQzAxWkRjM09XVTNZemN4TURjaUxDSmhaR1FpT2x0ZExDSnpkV0lpT2lKak1XVTVPVE01TkMwd09HVTJMVFJoTmpVdE9XRTJOaTFpTXpObU5qUmlNMkpoTnpRaUxDSnBjM01pT2lKdWIzTjBZV3h2WjJsakxtNWxkQ0lzSW1WNGNDSTZNVFl3TkRjd05EVTBNeXdpYVdGMElqb3hOakEwTURrNU56UXpMQ0puZENJNklsVlRSVkpPUVUxRkluMC5YM0Q5X3NCbmdHMnVnRDJkZndjNEtvUjZaaklPM3pNN0RreUQ3VzN5Vm1ldFJqcTh6T3E0MzhtRGE3ZVAyNFZKVmpYa29mcEZDZ2tfZWoxd3pUQ2ZBdw==")

    }

    @Test
    fun `Refresh a login`() {
        val session = doLogin()
        val headers = HttpHeaders()
        headers.set(NoStrings.AUTH_HEADER, session.token)
        val grant = LoginGrant(OWNER_ID, emptySet(), endDate, EntityUtils.uuid(), AuthenticationType.USERNAME)
        val token = TokenEncoder.encodeLoginGrant(grant)
        every { accessComms.refreshSession(ofType(String::class)) } answers {
            SessionSummary(EntityUtils.uuid(), OWNER_ID, grant.type, NoDate(), endDate, null, token) }
        val exchange = exchange(
                entity = HttpEntity<Unit>(headers),
                responseType = object : ParameterizedTypeReference<AuthenticationResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${AuthenticationController.AUTH_ENDPOINT}${AuthenticationController.REFRESH_URI}").body!!
        Assertions.assertTrue(exchange.authenticated)
    }

    @Test
    fun `Normal log out`() {
        val session = doLogin()
        val headers = HttpHeaders()
        headers.set(NoStrings.AUTH_HEADER, session.token)
        every { accessComms.endSession(ofType(String::class)) } answers {
            SessionSummary(EntityUtils.uuid(), OWNER_ID, AuthenticationType.USERNAME, NoDate(), endDate, null, null) }
        val exchange = exchange(
                entity = HttpEntity<Unit>(headers),
                responseType = object : ParameterizedTypeReference<AuthenticationResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${AuthenticationController.AUTH_ENDPOINT}${AuthenticationController.LOGOUT_URI}").body!!
        Assertions.assertNotNull(exchange)
        Assertions.assertFalse(exchange.authenticated)
        Assertions.assertNull(exchange.token)
    }

    @Test
    fun `Create a new password with an old password`() {
        val grant = LoginGrant(OWNER_ID, emptySet(), endDate, EntityUtils.uuid(), AuthenticationType.USERNAME)
        val token = TokenEncoder.encodeLoginGrant(grant)
        every { accessComms.createSession(ofType(SessionPrompt::class)) } answers {
            SessionSummary(EntityUtils.uuid(), OWNER_ID, grant.type, NoDate(), endDate, null, token) }
        val exchange = exchange(
                entity = HttpEntity(LoginRequest(OWNER_NAME, OWNER_PASSWORD, "NewOwnerPassword")),
                responseType = object : ParameterizedTypeReference<AuthenticationResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${AuthenticationController.AUTH_ENDPOINT}${AuthenticationController.PASSWORD_URI}").body!!
        Assertions.assertTrue(exchange.authenticated)
    }

}
