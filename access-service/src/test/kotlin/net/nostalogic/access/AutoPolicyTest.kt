package net.nostalogic.access

import net.nostalogic.access.config.TestAccessDbContainer
import net.nostalogic.access.testutils.TestUtils
import net.nostalogic.config.Config
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.datamodel.NamedEntity
import net.nostalogic.datamodel.Setting
import net.nostalogic.datamodel.access.EntityPermission
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.datamodel.access.PolicyPriority
import net.nostalogic.entities.EntitySignature
import net.nostalogic.entities.NoEntity
import net.nostalogic.utils.AutoPolicy
import net.nostalogic.utils.EntityUtils
import org.junit.ClassRule
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
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
class AutoPolicyTest(@Autowired val dbLoader: DatabaseLoader) {

    companion object {
        @JvmField
        @ClassRule
        var postgreSQLContainer: PostgreSQLContainer<*> = TestAccessDbContainer.getInstance()

        @JvmStatic
        @BeforeAll
        fun beforeAll(): Unit {
            postgreSQLContainer.start()
        }
    }

    private val localhost = "http://localhost:"

    private val TEST_USER = EntitySignature("5f086280-32d2-4955-9874-0a9d8ee3ca88", NoEntity.USER)
    private val TEST_GROUP = EntitySignature(EntityUtils.uuid(), NoEntity.GROUP)

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
    fun `Create an auto policy`() {
        val policy = AutoPolicy.generate(TEST_GROUP, TEST_USER.id, PolicyAction.READ, setOf(TEST_GROUP.toEntityReference()), true)
        assertNotNull(policy)
        assertEquals("AUTO - ${TEST_GROUP} - READ", policy.name)
        assertEquals(PolicyPriority.TWO_STANDARD, policy.priority)
        assertEquals(1, policy.resources!!.size)
        assertEquals(TEST_GROUP.toString(), policy.resources!!.iterator().next())
        assertEquals(1, policy.subjects!!.size)
        assertEquals(TEST_GROUP.toString(), policy.subjects!!.iterator().next())
        assertEquals(1, policy.permissions!!.size)
        assertTrue(policy.permissions!![PolicyAction.READ]!!)
    }

    @Test
    fun `Retrieve auto policies`() {
        val generated = AutoPolicy.generate(TEST_GROUP, TEST_USER.id, PolicyAction.READ, setOf(TEST_GROUP.toEntityReference()), true)
        val policies = AutoPolicy.retrieve(TEST_GROUP, setOf(PolicyAction.READ))
        assertNotNull(policies)
        assertEquals(1, policies.size)
        TestUtils.assertPoliciesEqual(generated, policies.iterator().next(), true)
    }

    @Test
    fun `Remove auto policies`() {
        val generated = AutoPolicy.generate(TEST_GROUP, TEST_USER.id, PolicyAction.READ, setOf(TEST_GROUP.toEntityReference()), true)
        var policies = AutoPolicy.retrieve(TEST_GROUP, setOf(PolicyAction.READ))
        assertEquals(1, policies.size)
        AutoPolicy.delete(generated.id!!)
        policies = AutoPolicy.retrieve(TEST_GROUP, setOf(PolicyAction.READ))
        assertEquals(0, policies.size)
    }

    @Test
    fun `Save new permissions test`() {
        val permissions = hashSetOf(EntityPermission(PolicyAction.READ, false, hashSetOf(NamedEntity(null, TEST_GROUP.id, NoEntity.GROUP))))
        AutoPolicy.savePermissions(TEST_GROUP, TEST_USER.id, permissions)
        val returnedPermissions = AutoPolicy.retrievePermissions(TEST_GROUP)
        assertEquals(permissions, returnedPermissions)
    }

    @Test
    fun `Save changed permissions test`() {
        val original = hashSetOf(EntityPermission(PolicyAction.READ, false, hashSetOf(NamedEntity(null, TEST_GROUP.id, NoEntity.GROUP))))
        AutoPolicy.savePermissions(TEST_GROUP, TEST_USER.id, original)
        val updated = hashSetOf(
                EntityPermission(PolicyAction.READ, false,
                        hashSetOf(NamedEntity(null, EntityUtils.uuid(), NoEntity.USER),
                                NamedEntity(null, EntityUtils.uuid(), NoEntity.USER))),
                EntityPermission(PolicyAction.EDIT, false, hashSetOf(NamedEntity(null, EntityUtils.uuid(), NoEntity.USER))))
        AutoPolicy.savePermissions(TEST_GROUP, TEST_USER.id, updated)
        val returnedPermissions = AutoPolicy.retrievePermissions(TEST_GROUP)
        assertEquals(updated, returnedPermissions)
    }

}
