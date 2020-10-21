package net.nostalogic.excomm.controllers

import net.nostalogic.comms.ExcommComms
import net.nostalogic.config.Config
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.constants.MessageType
import net.nostalogic.constants.NoLocale
import net.nostalogic.datamodel.Setting
import net.nostalogic.datamodel.excomm.MessageOutline
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.EntitySignature
import net.nostalogic.entities.NoEntity
import net.nostalogic.excomm.ExcommApplication
import net.nostalogic.excomm.persistence.repositories.EmailRepository
import net.nostalogic.utils.EntityUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [ExcommApplication::class])
class ExcommControllerTest(@Autowired val dbLoader: DatabaseLoader, @Autowired val emailRepository: EmailRepository) {

    private val localhost = "http://localhost:"

    @Value("\${local.server.port}")
    var port: Int? = null
    var baseApiUrl = localhost

    private val recipientId = EntityUtils.uuid()
    private val recipientEmail = "test.recipient@nostalogic.net"
    private val testCode = "test-code"

    @BeforeEach
    fun setup() {
        baseApiUrl = localhost + port
        dbLoader.runDbCleanSetup()
        Config.addSetting(Setting("microservices.excomm-port", ":$port", Setting.Source.SERVICE))
    }

    @AfterEach
    fun teardown() {
        dbLoader.runDataWipeScripts()
        dbLoader.runSchemaDropScripts()
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

    @Test
    fun `Send confirm registration email`() {
        val outline = MessageOutline(
                recipientId = recipientId,
                recipientEmailAddress = recipientEmail,
                type = MessageType.REGISTRATION_CONFIRM,
                locale = NoLocale.en_GB)
        outline.setParameter("reg_code", testCode)
        val exchange = exchange(
                entity = HttpEntity(outline),
                responseType = object: ParameterizedTypeReference<String> () {},
                method = HttpMethod.POST,
                url = "$localhost$port${ExcommController.EXCOMM_ENDPOINT}${ExcommController.MESSAGE_URI}")
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertNotNull(exchange.body)
        val entityId = EntityReference(exchange.body!!)
        Assertions.assertEquals(NoEntity.EMAIL, entityId.entity)

        val emails = emailRepository.findAll()
        Assertions.assertEquals(1, emails.size)
        val email = emails.iterator().next()
        val dbId = EntitySignature(email.id, NoEntity.EMAIL)
        Assertions.assertEquals(entityId.toString(), dbId.toString())
        Assertions.assertTrue(email.bodyHtml.contains("<a href=\"http://localhost:3331/regconfirm?code=test-code\">"))
        Assertions.assertTrue(email.bodyPlain.contains("http://localhost:3331/regconfirm?code=test-code"))
    }

    @Test
    fun `Internal excomm comms works properly`() {
        val id = ExcommComms.send(MessageOutline(
                recipientId = recipientId,
                recipientEmailAddress = recipientEmail,
                type = MessageType.REGISTRATION_CONFIRM,
                locale = NoLocale.en_GB))
        Assertions.assertNotNull(id)
        val entityId = EntityReference(id!!)
        Assertions.assertEquals(NoEntity.EMAIL, entityId.entity)
    }

}
