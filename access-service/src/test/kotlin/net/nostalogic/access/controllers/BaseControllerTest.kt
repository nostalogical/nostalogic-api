package net.nostalogic.access.controllers

import net.nostalogic.access.AccessApplication
import net.nostalogic.config.Config
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.datamodel.Setting
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.datamodel.access.PolicyPriority
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.EntitySignature
import net.nostalogic.entities.NoEntity
import net.nostalogic.utils.CollUtils
import net.nostalogic.utils.EntityUtils
import org.apache.commons.lang3.RandomUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate
import java.io.IOException

@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [AccessApplication::class])
abstract class BaseControllerTest(@Autowired val dbLoader: DatabaseLoader) {

    companion object {
        val TEST_USER = EntitySignature("5f086280-32d2-4955-9874-0a9d8ee3ca88", NoEntity.USER)
        val TEST_USER_GROUP = EntitySignature("d8d6a9c4-b9ce-4660-b037-2d6d330da846", NoEntity.GROUP)
    }

    private val localhost = "http://localhost:"

    @Value("\${local.server.port}")
    var port: Int? = null
    var baseApiUrl = localhost

    @BeforeEach
    fun setup() {
        baseApiUrl = localhost + port
        dbLoader.runDbCleanSetup()
        Config.addSetting(Setting("microservices.access-port", ":$port", Setting.Source.SERVICE))
    }

    @AfterEach
    fun teardown() {
        dbLoader.runDataWipeScripts()
        dbLoader.runSchemaDropScripts()
    }

    fun accessUrl(): String {
        return baseApiUrl + AccessController.ACCESS_ENDPOINT
    }

    fun policyUrl(): String {
        return accessUrl() + AccessController.POLICIES_URI
    }

    fun testPolicy(): Policy {
        return Policy(
                name = "Test Policy " + RandomUtils.nextInt(0, 10000),
                priority = PolicyPriority.TWO_STANDARD,
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT_OWN, true)),
                resources = hashSetOf(rndResource()),
                subjects = hashSetOf(rndSubject()),
                creator = TEST_USER.id
        )
    }

    fun createPolicy(policy: Policy): Policy {
        val exchange = exchange(
                entity = HttpEntity(policy),
                responseType = object : ParameterizedTypeReference<Policy>() {},
                method = HttpMethod.POST,
                url = policyUrl()
        )
        Assertions.assertEquals(HttpStatus.CREATED, exchange.statusCode)
        Assertions.assertNotNull(exchange.body)
        return exchange.body!!
    }

    protected fun createTemplate(): RestTemplate {
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

    fun rndResource(): String {
        val entities = hashSetOf(
                NoEntity.ARTICLE,
                NoEntity.EMAIL,
                NoEntity.GROUP,
                NoEntity.USER,
                NoEntity.NAV,
                NoEntity.POLICY,
                NoEntity.SESSION)
        return EntityReference(EntityUtils.uuid(), entities.random()).toString()
    }

    fun rndSubject(): String {
        val entities = hashSetOf(NoEntity.USER, NoEntity.GROUP)
        return EntityReference(EntityUtils.uuid(), entities.random()).toString()
    }

}
