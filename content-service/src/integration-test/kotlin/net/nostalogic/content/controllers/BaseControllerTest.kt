package net.nostalogic.content.controllers

import io.mockk.every
import io.mockk.mockk
import net.nostalogic.comms.AccessComms
import net.nostalogic.comms.Comms
import net.nostalogic.comms.ExcommComms
import net.nostalogic.config.Config
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.NoStrings
import net.nostalogic.content.ContentApplication
import net.nostalogic.content.config.ContentLoader
import net.nostalogic.datamodel.ErrorResponse
import net.nostalogic.datamodel.NoDate
import net.nostalogic.datamodel.Setting
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.AccessReport
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.NoEntity
import net.nostalogic.security.grants.LoginGrant
import net.nostalogic.security.utils.TokenEncoder
import net.nostalogic.utils.EntityUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
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
import java.time.temporal.ChronoUnit

@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [ContentApplication::class])
open class BaseControllerTest(@Autowired val dbLoader: DatabaseLoader, @Autowired val contentLoader: ContentLoader) {

    private val localhost = "http://localhost:"
    protected val accessComms: AccessComms = mockk()
    protected val excommComms: ExcommComms = mockk()

    @Value("\${local.server.port}")
    var port: Int? = null
    var baseApiUrl = localhost

    @BeforeEach
    fun setup() {
        baseApiUrl = localhost + port
        dbLoader.runDbCleanSetup()
        contentLoader.runResourceScripts()
        Config.addSetting(Setting("microservices.user-port", ":$port", Setting.Source.SERVICE))
        Comms.accessComms = this.accessComms
        Comms.excommComms = this.excommComms
    }

    @AfterEach
    fun teardown() {
        dbLoader.runDataWipeScripts()
        dbLoader.runSchemaDropScripts()
    }

    fun <T> exchange(entity: HttpEntity<*>, responseType: ParameterizedTypeReference<T>, method: HttpMethod, url: String): ResponseEntity<T> {
        try {
            return createTemplate().exchange(url, method, entity, responseType)
        } catch (e: Exception) {
            val unknownResponse = createTemplate().exchange(url, method, entity, object : ParameterizedTypeReference<Any>() {})
            println("\nUnknown request response, expected:")
            println(responseType.type.typeName)
            println("Actual response:")
            Assertions.fail<Any>(unknownResponse.body.toString())
            throw e
        }
    }

    fun exchangeError(entity: HttpEntity<*>, method: HttpMethod, url: String): ResponseEntity<ErrorResponse> {
        return exchange(entity, object : ParameterizedTypeReference<ErrorResponse>() {}, method, url)
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

    protected fun testHeaders(userId: String? = null): HttpHeaders {
        val headers = HttpHeaders()
        if (userId != null)
            headers.set(
                NoStrings.AUTH_HEADER, TokenEncoder.encodeLoginGrant(
                    LoginGrant(
                subject = userId, expiration = NoDate.plus(1, ChronoUnit.MINUTES),
                sessionId = EntityUtils.uuid(), type = AuthenticationType.USERNAME)
                ))
        return headers
    }

    protected fun mockPermissions(resourcePermissions: HashMap<String, HashMap<PolicyAction, Boolean>> = HashMap(),
                                  entityPermissions: HashMap<NoEntity, HashMap<PolicyAction, Boolean>> = HashMap()
    ) {
        every { accessComms.query(ofType(AccessQuery::class)) } answers {
            AccessReport(entityPermissions = entityPermissions, resourcePermissions = resourcePermissions) }
    }

}
