package net.nostalogic.users.controllers

import io.mockk.mockk
import net.nostalogic.comms.AccessComms
import net.nostalogic.comms.Comms
import net.nostalogic.comms.ExcommComms
import net.nostalogic.config.Config
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.datamodel.Setting
import net.nostalogic.users.UsersApplication
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [UsersApplication::class])
open class BaseControllerTest(@Autowired val dbLoader: DatabaseLoader) {

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

}
