package net.nostalogic.access.controllers

import net.nostalogic.access.AccessApplication
import net.nostalogic.access.datamodel.ResourcePermissionContext
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.AccessReport
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.datamodel.access.PolicyPriority
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.EntitySignature
import net.nostalogic.entities.NoEntity
import org.junit.jupiter.api.Assertions
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

@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [AccessApplication::class])
class AccessControllerQueryTest(@Autowired dbLoader: DatabaseLoader) : BaseControllerTest(dbLoader) {

    companion object {
        private val TEST_USER = EntitySignature("5f086280-32d2-4955-9874-0a9d8ee3ca88", NoEntity.USER)
        private val TEST_USER_GROUP = EntitySignature("d8d6a9c4-b9ce-4660-b037-2d6d330da846", NoEntity.GROUP)
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
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertNotNull(exchange.body)
        return exchange.body!!
    }

    private fun query(query: AccessQuery): AccessReport {
        val exchange = exchange(entity = HttpEntity(query),
                responseType = object: ParameterizedTypeReference<AccessReport> () {},
                method = HttpMethod.POST, url = queryUrl())
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertNotNull(exchange.body)
        return exchange.body!!
    }

    private fun assertPolicyApplied(context: ResourcePermissionContext, policyId: String, name: String, signatureLevel: Boolean, vararg subjects: String) {
        Assertions.assertTrue(context.policies.containsKey(policyId))
        val details = context.policies[policyId]
        Assertions.assertEquals(name, details!!.name)
        Assertions.assertTrue(details.resources.contains(if (signatureLevel) context.resource.toString() else (context.resource.entity.name)))
        for (sub in subjects)
            Assertions.assertTrue(details.subjects.contains(sub), "Expected the policy subjects to include $sub")
    }

    private fun assertContext(context: ResourcePermissionContext, resource: EntityReference,
                              action: PolicyAction, priority: PolicyPriority?, allow: Boolean) {
        Assertions.assertEquals(resource, context.resource)
        Assertions.assertEquals(action, context.action)
        Assertions.assertEquals(priority, context.priority)
        Assertions.assertEquals(allow, context.allow)
    }

    @Suppress("DuplicatedCode", "MapGetWithNotNullAssertionOperator")
    private fun assertReport(accessReport: AccessReport, subjects: HashSet<String>,
                             resources: HashMap<String, HashMap<PolicyAction, Boolean>>?,
                             entities: HashMap<NoEntity, HashMap<PolicyAction, Boolean>>?) {
        Assertions.assertEquals(subjects, accessReport.subjectIds)
        if (resources == null)
            Assertions.assertTrue(accessReport.resourcePermissions.isEmpty())
        else {
            for (resource in resources) {
                Assertions.assertTrue(accessReport.resourcePermissions.containsKey(resource.key))
                for (action in resource.value) {
                    Assertions.assertEquals(action.value, accessReport.resourcePermissions[resource.key]!![action.key])
                }
            }
        }
        if (entities == null)
            Assertions.assertTrue(accessReport.entityPermissions.isEmpty())
        else {
            for (entity in entities) {
                Assertions.assertTrue(accessReport.entityPermissions.containsKey(entity.key))
                for (action in entity.value) {
                    Assertions.assertEquals(action.value, accessReport.entityPermissions[entity.key]!![action.key])
                }
            }
        }
    }

    private fun assertSingleContext(contexts: HashSet<ResourcePermissionContext>): ResourcePermissionContext {
        Assertions.assertEquals(1, contexts.size)
        return contexts.iterator().next()
    }

    @Test
    fun `Confirm read access on navs`() {
        val contexts = analyse(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(NoEntity.NAV.name, hashSetOf(PolicyAction.READ)))))
        val context = assertSingleContext(contexts)
        assertContext(context, EntityReference(entity = NoEntity.NAV), PolicyAction.READ, PolicyPriority.LEVEL_ONE, true)
        assertPolicyApplied(context, "6aac60f8-1b4d-430e-911f-a86caa8ec1ba", "Default Read", false, "ALL")

        val report = query(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(NoEntity.NAV.name, hashSetOf(PolicyAction.READ)))))
        assertReport(report, hashSetOf(TEST_USER.toString()), null, hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.READ, true)))))
    }

    @Test
    fun `Confirm deny policies take precedence`() {
        val equalOppositeEntity = EntityReference("b332223b-32bf-42b7-bb07-24174516a410", NoEntity.ARTICLE)
        val contexts = analyse(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(equalOppositeEntity.toString(), hashSetOf(PolicyAction.READ)))))
        val context = assertSingleContext(contexts)
        assertContext(context, equalOppositeEntity, PolicyAction.READ, PolicyPriority.LEVEL_ONE, false)
        assertPolicyApplied(context, "93b8b29f-ad3f-4697-a716-1c56fe9d93d8", "Equal priority opposite effect 2", true, TEST_USER.toString())

        val report = query(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(equalOppositeEntity.toString(), hashSetOf(PolicyAction.READ)))))
        assertReport(report, hashSetOf(TEST_USER.toString()), hashMapOf(Pair(equalOppositeEntity.toString(), hashMapOf(Pair(PolicyAction.READ, false)))), null)
    }

    @Test
    fun `Confirm higher priority policies take precedence`() {
        val higherPriorityEntity = EntityReference("04a20bb8-4d01-4b7e-91d9-ce9314d0bfbb", NoEntity.NAV)
        val contexts = analyse(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(higherPriorityEntity.toString(), hashSetOf(PolicyAction.EDIT)))))
        val context = assertSingleContext(contexts)
        assertContext(context, higherPriorityEntity, PolicyAction.EDIT, PolicyPriority.LEVEL_THREE, true)
        assertPolicyApplied(context, "53d2ed24-bec0-41f1-9609-1532b0849102", "High priority test 2", true, TEST_USER.toString())

        val report = query(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(higherPriorityEntity.toString(), hashSetOf(PolicyAction.EDIT)))))
        assertReport(report, hashSetOf(TEST_USER.toString()), hashMapOf(Pair(higherPriorityEntity.toString(), hashMapOf(Pair(PolicyAction.EDIT, true)))), null)
    }

    @Test
    fun `All equal result policies on a resource are reported`() {
        val equalResults = EntityReference("20590221-63d1-4750-873d-916e24900406", NoEntity.NAV)
        val contexts = analyse(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(equalResults.toString(), hashSetOf(PolicyAction.DELETE)))))
        val context = assertSingleContext(contexts)
        assertContext(context, equalResults, PolicyAction.DELETE, PolicyPriority.LEVEL_ONE, true)
        assertPolicyApplied(context, "f2adeff6-912d-4022-b5b4-267ecef4beee", "Equal result for resource 1", true, TEST_USER.toString())
        assertPolicyApplied(context, "c013563b-6480-43e4-bc1b-c457d389df95", "Equal result for resource 2", true, TEST_USER.toString())

        val report = query(AccessQuery(hashSetOf(TEST_USER.toString()), hashMapOf(Pair(equalResults.toString(), hashSetOf(PolicyAction.DELETE)))))
        assertReport(report, hashSetOf(TEST_USER.toString()), hashMapOf(Pair(equalResults.toString(), hashMapOf(Pair(PolicyAction.DELETE, true)))), null)
    }

    @Test
    fun `All equal result policies for multiple subjects are reported`() {
        val multipleSubjects = EntityReference("e6285bef-d8d1-461a-9507-f9b8a7426b3e", NoEntity.CONTAINER)
        val contexts = analyse(AccessQuery(hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(Pair(multipleSubjects.toString(), hashSetOf(PolicyAction.DELETE)))))
        val context = assertSingleContext(contexts)
        assertContext(context, multipleSubjects, PolicyAction.DELETE, PolicyPriority.LEVEL_ONE, true)
        assertPolicyApplied(context, "35929e52-4f15-4f41-a445-e4e68ef3f30e", "Multiple subjects report 1", true, TEST_USER.toString())
        assertPolicyApplied(context, "1f88a5b8-55af-47a9-b43d-c22da8d1721a", "Multiple subjects report 2", false, TEST_USER_GROUP.toString())

        val report = query(AccessQuery(hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(Pair(multipleSubjects.toString(), hashSetOf(PolicyAction.DELETE)))))
        assertReport(report, hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(Pair(multipleSubjects.toString(), hashMapOf(Pair(PolicyAction.DELETE, true)))), null)
    }

    @Test
    fun `No applicable policies defaults to deny`() {
        val multipleSubjects = EntityReference("1c3c29b7-27c1-4a92-b976-57fb718c619a", NoEntity.POLICY)
        val contexts = analyse(AccessQuery(hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(Pair(multipleSubjects.toString(), hashSetOf(PolicyAction.DELETE)))))
        val context = assertSingleContext(contexts)
        assertContext(context, multipleSubjects, PolicyAction.DELETE, null, false)
        Assertions.assertTrue(context.policies.isEmpty())

        val report = query(AccessQuery(hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(Pair(multipleSubjects.toString(), hashSetOf(PolicyAction.DELETE)))))
        assertReport(report, hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(Pair(multipleSubjects.toString(), hashMapOf(Pair(PolicyAction.DELETE, false)))), null)
    }

    @Test
    fun `Multiple actions can be queried`() {
        val container1 = EntityReference("e6285bef-d8d1-461a-9507-f9b8a7426b3e", NoEntity.CONTAINER)
        val nav1 = EntityReference("04a20bb8-4d01-4b7e-91d9-ce9314d0bfbb", NoEntity.NAV)
        val contexts = analyse(AccessQuery(hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(
                        Pair(container1.toString(), hashSetOf(PolicyAction.DELETE, PolicyAction.READ)),
                        Pair(nav1.toString(), hashSetOf(PolicyAction.EDIT))
                )))
        var contextCount = 0
        for (context in contexts) {
            if (context.resource == container1 && context.action == PolicyAction.DELETE) {
                assertContext(context, container1, PolicyAction.DELETE, PolicyPriority.LEVEL_ONE, true)
                assertPolicyApplied(context, "35929e52-4f15-4f41-a445-e4e68ef3f30e", "Multiple subjects report 1", true, TEST_USER.toString())
                assertPolicyApplied(context, "1f88a5b8-55af-47a9-b43d-c22da8d1721a", "Multiple subjects report 2", false, TEST_USER_GROUP.toString())
                contextCount += 1
                continue
            } else if (context.resource == container1 && context.action == PolicyAction.READ) {
                assertContext(context, container1, PolicyAction.READ, null, false)
                Assertions.assertTrue(context.policies.isEmpty())
                contextCount += 1
                continue
            } else if (context.resource == nav1 && context.action == PolicyAction.EDIT) {
                assertContext(context, nav1, PolicyAction.EDIT, PolicyPriority.LEVEL_THREE, true)
                assertPolicyApplied(context, "53d2ed24-bec0-41f1-9609-1532b0849102", "High priority test 2", true, TEST_USER.toString())
                contextCount += 1
                continue
            }
        }
        Assertions.assertEquals(3, contextCount)

        val report = query(AccessQuery(hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(
                        Pair(container1.toString(), hashSetOf(PolicyAction.DELETE, PolicyAction.READ)),
                        Pair(nav1.toString(), hashSetOf(PolicyAction.EDIT))
                )))
        assertReport(report, hashSetOf(TEST_USER.toString(), TEST_USER_GROUP.toString()),
                hashMapOf(
                        Pair(container1.toString(), hashMapOf(Pair(PolicyAction.DELETE, true), Pair(PolicyAction.READ, false))),
                        Pair(nav1.toString(), hashMapOf(Pair(PolicyAction.EDIT, true)))
                ), null)
    }

}
