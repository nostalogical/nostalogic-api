package net.nostalogic

import net.nostalogic.access.AccessApplication
import net.nostalogic.access.testutils.TestUtils
import net.nostalogic.comms.AccessComms
import net.nostalogic.config.Config
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.Setting
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntitySignature
import net.nostalogic.entities.NoEntity
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.utils.EntityUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [AccessApplication::class])
class CommsTest(@Autowired val dbLoader: DatabaseLoader) {

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
        val summary = AccessComms.createSession(SessionPrompt(TEST_USER.id, emptySet(), AuthenticationType.EMAIL))
        Assertions.assertNotNull(summary)
        Assertions.assertEquals(TEST_USER.id, summary!!.userId)
        Assertions.assertNotNull(summary.token)
    }

    @Test
    fun `Refresh session comms works`() {
        val creation = AccessComms.createSession(SessionPrompt(TEST_USER.id, emptySet(), AuthenticationType.EMAIL))
        Assertions.assertNotNull(creation)
        val summary = AccessComms.refreshSession(creation!!.token!!)
        Assertions.assertNotNull(summary)
        Assertions.assertEquals(TEST_USER.id, summary!!.userId)
        Assertions.assertNotNull(summary.token)
    }

    @Test
    fun `End session comms works`() {
        val creation = AccessComms.createSession(SessionPrompt(TEST_USER.id, emptySet(), AuthenticationType.EMAIL))
        Assertions.assertNotNull(creation)
        val summary = AccessComms.endSession(creation!!.token!!)
        Assertions.assertEquals(TEST_USER.id, summary!!.userId)
    }

    @Test
    fun `Verify session comms works`() {
        val creation = AccessComms.createSession(SessionPrompt(TEST_USER.id, emptySet(), AuthenticationType.EMAIL))
        Assertions.assertNotNull(creation)
        val summary = AccessComms.verifySession(creation!!.token!!)
        Assertions.assertNotNull(summary!!.token)
    }

    @Test
    fun `Update session comms works`() {
        val creation = AccessComms.createSession(SessionPrompt(TEST_USER.id, emptySet(), AuthenticationType.EMAIL))
        Assertions.assertNotNull(creation)
        val groups = setOf(EntityUtils.uuid(), EntityUtils.uuid())
        val summary = AccessComms.updateSession(groups, TEST_USER.id)
        Assertions.assertNotNull(summary)
    }

}
