package net.nostalogic.access

import net.nostalogic.access.config.TestAccessDbContainer
import net.nostalogic.access.testutils.TestUtils
import net.nostalogic.comms.AccessComms
import net.nostalogic.config.Config
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.constants.AuthenticationSource
import net.nostalogic.datamodel.Setting
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntitySignature
import net.nostalogic.entities.NoEntity
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.utils.EntityUtils
import org.junit.ClassRule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.PostgreSQLContainer


@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [AccessApplication::class])
class CommsTest(@Autowired val dbLoader: DatabaseLoader) {

    companion object {
        @JvmField
        @ClassRule
        var postgreSQLContainer: PostgreSQLContainer<*> = TestAccessDbContainer.getInstance()

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            postgreSQLContainer.start()
        }
    }

    private val localhost = "http://localhost:"

    private val TEST_USER = EntitySignature("5f086280-32d2-4955-9874-0a9d8ee3ca88", NoEntity.USER)

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

    @Test
    fun `Internal access comms works`() {
        val report = AccessComms.query(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(NoEntity.NAV.name, hashSetOf(PolicyAction.READ)))))
        TestUtils.assertReport(report, hashSetOf(TEST_USER.toString()), null, hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.READ, true)))))
    }

    @Test
    fun `Create session comms works`() {
        val summary = AccessComms.createSession(SessionPrompt(TEST_USER.id, AuthenticationSource.EMAIL))
        assertNotNull(summary)
        assertEquals(TEST_USER.id, summary!!.userId)
        assertNotNull(summary.accessToken)
    }

    @Test
    fun `Refresh session comms works`() {
        val creation = AccessComms.createSession(SessionPrompt(TEST_USER.id, AuthenticationSource.EMAIL))
        assertNotNull(creation)
        val summary = AccessComms.refreshSession(creation!!.refreshToken!!.token)
        assertNotNull(summary)
        assertEquals(TEST_USER.id, summary!!.userId)
        assertNotNull(summary.accessToken)
    }

    @Test
    fun `End session comms works`() {
        val creation = AccessComms.createSession(SessionPrompt(TEST_USER.id, AuthenticationSource.EMAIL))
        assertNotNull(creation)
        val summary = AccessComms.endSession(creation!!.accessToken!!.token)
        assertEquals(TEST_USER.id, summary!!.userId)
    }

    @Test
    fun `Verify session comms works`() {
        val creation = AccessComms.createSession(SessionPrompt(TEST_USER.id, AuthenticationSource.EMAIL))
        assertNotNull(creation)
        val summary = AccessComms.verifySession(creation!!.accessToken!!.token)
        assertNotNull(summary)
        assertEquals(creation.sessionId, summary?.sessionId)
        assertNull(summary!!.accessToken, "No new tokens should be returned when verifying a session")
        assertNull(summary.refreshToken, "No new tokens should be returned when verifying a session")
    }

    @Test
    fun `Update session comms works`() {
        val creation = AccessComms.createSession(SessionPrompt(TEST_USER.id, AuthenticationSource.EMAIL))
        assertNotNull(creation)
        val groups = hashSetOf(EntityUtils.uuid(), EntityUtils.uuid())
        val summary = AccessComms.updateSession(groups, TEST_USER.id)
        assertNotNull(summary)
    }

}
