package net.nostalogic.access.controllers

import net.nostalogic.access.AccessApplication
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity
import net.nostalogic.utils.EntityUtils
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

    val localhost = "http://localhost:"

    @Value("\${local.server.port}")
    var port: Int? = null
    var baseApiUrl = localhost

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
                NoEntity.CONTAINER,
                NoEntity.EMAIL,
                NoEntity.GROUP,
                NoEntity.USER,
                NoEntity.NAV,
                NoEntity.POLICY,
                NoEntity.SESSION)
        return EntityReference(EntityUtils.uuid(), entities.random()).toFullId()
    }

    fun rndSubject(): String {
        val entities = hashSetOf(NoEntity.USER, NoEntity.GROUP)
        return EntityReference(EntityUtils.uuid(), entities.random()).toFullId()
    }

}
