package net.nostalogic.access.controllers

import net.nostalogic.access.AccessApplication
import net.nostalogic.access.config.TestAccessDbContainer
import net.nostalogic.access.datamodel.ResourcePermissionContext
import net.nostalogic.access.testutils.TestUtils
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.AccessReport
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.datamodel.access.PolicyPriority
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.EntitySignature
import net.nostalogic.entities.NoEntity
import org.junit.ClassRule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.PostgreSQLContainer

@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [AccessApplication::class])
class AccessControllerQueryTest(@Autowired dbLoader: DatabaseLoader) : BaseControllerTest(dbLoader) {

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

    private fun analyseUrl(): String {
        return baseApiUrl + AccessController.ACCESS_ENDPOINT + AccessController.ANALYSE_URI
    }

    private fun queryUrl(): String {
        return baseApiUrl + AccessController.ACCESS_ENDPOINT
    }

    private fun analyse(query: AccessQuery): HashSet<ResourcePermissionContext> {
        val exchange = exchange(entity = HttpEntity(query),
                responseType = object: ParameterizedTypeReference<HashSet<ResourcePermissionContext>> () {},
                method = HttpMethod.POST, url = analyseUrl())
        assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertNotNull(exchange.body)
        return exchange.body!!
    }

    private fun query(query: AccessQuery): AccessReport {
        val exchange = exchange(entity = HttpEntity(query),
                responseType = object: ParameterizedTypeReference<AccessReport> () {},
                method = HttpMethod.POST, url = queryUrl())
        assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertNotNull(exchange.body)
        return exchange.body!!
    }

    private fun assertPolicyApplied(context: ResourcePermissionContext, policyId: String, name: String, signatureLevel: Boolean, vararg subjects: String) {
        assertTrue(context.policies.containsKey(policyId))
        val details = context.policies[policyId]
        assertEquals(name, details!!.name)
        if (signatureLevel)
            assertTrue(details.resources.contains(context.resource.toString()))
        else
            assertTrue(details.resources.contains(context.resource.entity.name) || details.resources.contains(NoEntity.ALL.name))
        for (sub in subjects)
            assertTrue(details.subjects.contains(sub), "Expected the policy subjects to include $sub")
    }

    private fun assertContext(context: ResourcePermissionContext, resource: EntityReference,
                              action: PolicyAction, priority: PolicyPriority?, allow: Boolean) {
        assertEquals(resource, context.resource)
        assertEquals(action, context.action)
        assertEquals(allow, context.allow)
        assertEquals(priority, context.priority)
    }



    private fun assertSingleContext(contexts: HashSet<ResourcePermissionContext>): ResourcePermissionContext {
        assertEquals(1, contexts.size)
        return contexts.iterator().next()
    }

    @Test
    fun `Confirm read access on navs`() {
        val contexts = analyse(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(NoEntity.NAV.name, hashSetOf(PolicyAction.READ)))))
        val context = assertSingleContext(contexts)
        assertContext(context, EntityReference(entity = NoEntity.NAV), PolicyAction.READ, PolicyPriority.ONE_AUTO, true)
        assertPolicyApplied(context, "6aac60f8-1b4d-430e-911f-a86caa8ec1ba", "Default Read", false, "ALL")

        val report = query(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(NoEntity.NAV.name, hashSetOf(PolicyAction.READ)))))
        TestUtils.assertReport(report, hashSetOf(TEST_USER.toString()), null, hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.READ, true)))))
    }

    @Test
    fun `Confirm deny policies take precedence`() {
        val equalOppositeEntity = EntityReference("b332223b-32bf-42b7-bb07-24174516a410", NoEntity.ARTICLE)
        val contexts = analyse(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(equalOppositeEntity.toString(), hashSetOf(PolicyAction.READ)))))
        val context = assertSingleContext(contexts)
        assertContext(context, equalOppositeEntity, PolicyAction.READ, PolicyPriority.TWO_STANDARD, false)
        assertPolicyApplied(context, "93b8b29f-ad3f-4697-a716-1c56fe9d93d8", "Equal priority opposite effect 2", true, TEST_USER.toString())

        val report = query(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(equalOppositeEntity.toString(), hashSetOf(PolicyAction.READ)))))
        TestUtils.assertReport(report, hashSetOf(TEST_USER.toString()), hashMapOf(Pair(equalOppositeEntity.toString(), hashMapOf(Pair(PolicyAction.READ, false)))), null)
    }

    @Test
    fun `Confirm higher priority policies take precedence`() {
        val higherPriorityEntity = EntityReference("04a20bb8-4d01-4b7e-91d9-ce9314d0bfbb", NoEntity.NAV)
        val contexts = analyse(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(higherPriorityEntity.toString(), hashSetOf(PolicyAction.EDIT)))))
        val context = assertSingleContext(contexts)
        assertContext(context, higherPriorityEntity, PolicyAction.EDIT, PolicyPriority.THREE_HIGH, true)
        assertPolicyApplied(context, "53d2ed24-bec0-41f1-9609-1532b0849102", "High priority test 2", true, TEST_USER.toString())

        val report = query(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(higherPriorityEntity.toString(), hashSetOf(PolicyAction.EDIT)))))
        TestUtils.assertReport(report, hashSetOf(TEST_USER.toString()), hashMapOf(Pair(higherPriorityEntity.toString(), hashMapOf(Pair(PolicyAction.EDIT, true)))), null)
    }

    @Test
    fun `All equal result policies on a resource are reported`() {
        val equalResults = EntityReference("20590221-63d1-4750-873d-916e24900406", NoEntity.NAV)
        val contexts = analyse(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(equalResults.toString(), hashSetOf(PolicyAction.DELETE)))))
        val context = assertSingleContext(contexts)
        assertContext(context, equalResults, PolicyAction.DELETE, PolicyPriority.TWO_STANDARD, true)
        assertPolicyApplied(context, "f2adeff6-912d-4022-b5b4-267ecef4beee", "Equal result for resource 1", true, TEST_USER.toString())
        assertPolicyApplied(context, "c013563b-6480-43e4-bc1b-c457d389df95", "Equal result for resource 2", true, TEST_USER.toString())

        val report = query(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(equalResults.toString(), hashSetOf(PolicyAction.DELETE)))))
        TestUtils.assertReport(report, hashSetOf(TEST_USER.toString()), hashMapOf(Pair(equalResults.toString(), hashMapOf(Pair(PolicyAction.DELETE, true)))), null)
    }

    @Test
    fun `All equal result policies for multiple subjects are reported`() {
        val multipleSubjects = EntityReference("e6285bef-d8d1-461a-9507-f9b8a7426b3e", NoEntity.EMAIL)
        val contexts = analyse(AccessQuery(hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(Pair(multipleSubjects.toString(), hashSetOf(PolicyAction.DELETE)))))
        val context = assertSingleContext(contexts)
        assertContext(context, multipleSubjects, PolicyAction.DELETE, PolicyPriority.TWO_STANDARD, true)
        assertPolicyApplied(context, "35929e52-4f15-4f41-a445-e4e68ef3f30e", "Multiple subjects report 1", true, TEST_USER.toString())
        assertPolicyApplied(context, "1f88a5b8-55af-47a9-b43d-c22da8d1721a", "Multiple subjects report 2", false, TEST_USER_GROUP.toString())

        val report = query(AccessQuery(hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(Pair(multipleSubjects.toString(), hashSetOf(PolicyAction.DELETE)))))
        TestUtils.assertReport(report, hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(Pair(multipleSubjects.toString(), hashMapOf(Pair(PolicyAction.DELETE, true)))), null)
    }

    @Test
    fun `No applicable policies defaults to deny`() {
        val multipleSubjects = EntityReference("1c3c29b7-27c1-4a92-b976-57fb718c619a", NoEntity.POLICY)
        val contexts = analyse(AccessQuery(hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(Pair(multipleSubjects.toString(), hashSetOf(PolicyAction.DELETE)))))
        val context = assertSingleContext(contexts)
        assertContext(context, multipleSubjects, PolicyAction.DELETE, null, false)
        assertTrue(context.policies.isEmpty())

        val report = query(AccessQuery(hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(Pair(multipleSubjects.toString(), hashSetOf(PolicyAction.DELETE)))))
        TestUtils.assertReport(report, hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(Pair(multipleSubjects.toString(), hashMapOf(Pair(PolicyAction.DELETE, false)))), null)
    }

    @Test
    fun `Multiple actions can be queried`() {
        val email1 = EntityReference("e6285bef-d8d1-461a-9507-f9b8a7426b3e", NoEntity.EMAIL)
        val nav1 = EntityReference("04a20bb8-4d01-4b7e-91d9-ce9314d0bfbb", NoEntity.NAV)
        val contexts = analyse(AccessQuery(hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(
                        Pair(email1.toString(), hashSetOf(PolicyAction.DELETE, PolicyAction.READ)),
                        Pair(nav1.toString(), hashSetOf(PolicyAction.EDIT))
                )))
        var contextCount = 0
        for (context in contexts) {
            if (context.resource == email1 && context.action == PolicyAction.DELETE) {
                assertContext(context, email1, PolicyAction.DELETE, PolicyPriority.TWO_STANDARD, true)
                assertPolicyApplied(context, "35929e52-4f15-4f41-a445-e4e68ef3f30e", "Multiple subjects report 1", true, TEST_USER.toString())
                assertPolicyApplied(context, "1f88a5b8-55af-47a9-b43d-c22da8d1721a", "Multiple subjects report 2", false, TEST_USER_GROUP.toString())
                contextCount += 1
                continue
            } else if (context.resource == email1 && context.action == PolicyAction.READ) {
                assertContext(context, email1, PolicyAction.READ, PolicyPriority.ONE_AUTO, true)
                assertPolicyApplied(context, "6aac60f8-1b4d-430e-911f-a86caa8ec1ba", "Default Read", false)
                contextCount += 1
                continue
            } else if (context.resource == nav1 && context.action == PolicyAction.EDIT) {
                assertContext(context, nav1, PolicyAction.EDIT, PolicyPriority.THREE_HIGH, true)
                assertPolicyApplied(context, "53d2ed24-bec0-41f1-9609-1532b0849102", "High priority test 2", true, TEST_USER.toString())
                contextCount += 1
                continue
            }
        }
        assertEquals(3, contextCount)

        val report = query(AccessQuery(hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(
                        Pair(email1.toString(), hashSetOf(PolicyAction.DELETE, PolicyAction.READ)),
                        Pair(nav1.toString(), hashSetOf(PolicyAction.EDIT))
                )))
        TestUtils.assertReport(report, hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(
                        Pair(email1.toString(), hashMapOf(Pair(PolicyAction.DELETE, true), Pair(PolicyAction.READ, true))),
                        Pair(nav1.toString(), hashMapOf(Pair(PolicyAction.EDIT, true)))
                ), null)
    }



    @Test
    fun `Confirm an ALL policy applies to everything`() {
        val multipleSubjects = EntityReference("1c3c29b7-27c1-4a92-b976-57fb718c619a", NoEntity.USER)
        val contexts = analyse(AccessQuery(hashSetOf(EntitySignature("74790c40-30b2-462b-9125-ee38200b94cd", NoEntity.USER).toString()),
                hashMapOf(Pair(multipleSubjects.toString(), hashSetOf(PolicyAction.DELETE)))))
        val context = assertSingleContext(contexts)
        assertContext(context, multipleSubjects, PolicyAction.DELETE, PolicyPriority.FIVE_SUPER, true)
    }

}
