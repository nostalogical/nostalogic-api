package net.nostalogic.access.controllers

import net.nostalogic.access.AccessApplication
import net.nostalogic.access.testutils.TestUtils
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.datamodel.ErrorResponse
import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.datamodel.access.PolicyPriority
import net.nostalogic.entities.EntityStatus
import net.nostalogic.utils.CollUtils
import net.nostalogic.utils.EntityUtils
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
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [AccessApplication::class])
open class AccessControllerTest(@Autowired dbLoader: DatabaseLoader) : BaseControllerTest(dbLoader) {

    @Test
    fun `Create a policy`() {
        val policy = testPolicy()
        val response = createPolicy(policy)
        TestUtils.assertPoliciesEqual(policy, response, false)
    }

    @Test
    fun `Create a policy with empty lists`() {
        val policy = Policy(name = "Test blank", priority = PolicyPriority.TWO_STANDARD)
        policy.permissions = EnumMap(PolicyAction::class.java)
        policy.resources = HashSet()
        policy.subjects = HashSet()
        val response = createPolicy(policy)
        TestUtils.assertPoliciesEqual(policy, response, false)
    }

    @Test
    fun `Create a blank policy`() {
        val response = exchange(entity = HttpEntity(Policy()),
                responseType = object: ParameterizedTypeReference<ErrorResponse> () {},
                method = HttpMethod.POST, url = policyUrl())
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertTrue(response.body!!.userMessage.contains("name"))
        Assertions.assertTrue(response.body!!.userMessage.contains("priority"))
    }

    @Test
    fun `Edit a policy`() {
        val policy = createPolicy(testPolicy())
        val response = exchange(entity = HttpEntity(policy),
                responseType = object: ParameterizedTypeReference<Policy> () {},
                method = HttpMethod.PUT, url = policyUrl() + "/${policy.id}")
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        TestUtils.assertPoliciesEqual(policy, response.body!!)
    }

    @Test
    fun `Edit a nonexistent policy`() {
        val response = exchange(entity = HttpEntity(testPolicy()),
                responseType = object: ParameterizedTypeReference<ErrorResponse> () {},
                method = HttpMethod.PUT, url = policyUrl() + "/${EntityUtils.uuid()}")
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `Edit a policy with only specific fields`() {
        val policy = createPolicy(testPolicy())
        val edit = Policy(status = EntityStatus.INACTIVE, permissions = null, subjects = null, resources = null)
        val response = exchange(entity = HttpEntity(edit),
                responseType = object: ParameterizedTypeReference<Policy> () {},
                method = HttpMethod.PUT, url = policyUrl() + "/${policy.id}")
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        policy.status = EntityStatus.INACTIVE
        TestUtils.assertPoliciesEqual(policy, response.body!!)
    }

    @Test
    fun `Edit a policy with an invalid field`() {
        val policy = createPolicy(testPolicy())
        policy.name = "A long name over the one hundred character limit which should throw an exception unless I've increased the name limit"
        val response = exchange(entity = HttpEntity(policy),
                responseType = object: ParameterizedTypeReference<ErrorResponse> () {},
                method = HttpMethod.PUT, url = policyUrl() + "/${policy.id}")
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals(response.body!!.errorCode, 207002)
    }

    private fun assertPolicyUpdate(policy: Policy) {
        val update = exchange(entity = HttpEntity(policy),
                responseType = object: ParameterizedTypeReference<Policy> () {},
                method = HttpMethod.PUT, url = policyUrl() + "/${policy.id}")
        Assertions.assertEquals(HttpStatus.OK, update.statusCode)
        Assertions.assertNotNull(update.body)
        TestUtils.assertPoliciesEqual(policy, update.body!!)
        val retrieve = createTemplate().exchange(policyUrl() + "/${policy.id}", HttpMethod.GET, null, Policy::class.java)
        TestUtils.assertPoliciesEqual(policy, retrieve.body!!)
    }

    @Test
    fun `Edit a policy to have different subjects and resources`() {
        val policy = createPolicy(testPolicy())
        policy.resources = hashSetOf(rndResource(), rndResource(), rndResource())
        policy.subjects = hashSetOf(rndSubject(), rndSubject(), rndSubject(), rndSubject())
        assertPolicyUpdate(policy)
    }

    @Test
    fun `Edit all policy fields`() {
        val policy = createPolicy(testPolicy())
        policy.name = "Changed"
        policy.priority = PolicyPriority.THREE_HIGH
        policy.permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.CREATE, true), Pair(PolicyAction.DELETE_OWN, false))
        policy.status = EntityStatus.INACTIVE
        policy.resources!!.addAll(hashSetOf(rndResource(), rndResource()))
        policy.subjects!!.addAll(hashSetOf(rndSubject(), rndSubject()))
        assertPolicyUpdate(policy)
    }

    @Test
    fun `Delete a policy`() {
        val policy = createPolicy(testPolicy())
        val response = createTemplate().exchange(policyUrl() + "/${policy.id}", HttpMethod.DELETE, null, Object::class.java)
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val secondDelete = createTemplate().exchange(policyUrl() + "/${policy.id}", HttpMethod.DELETE, null, Object::class.java)
        Assertions.assertEquals(HttpStatus.OK, secondDelete.statusCode)
    }

    @Test
    fun `Delete a nonexistent policy`() {
        val response = createTemplate().exchange(policyUrl() + "/${EntityUtils.uuid()}", HttpMethod.DELETE, null, Object::class.java)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    private fun createPolicies(count: Int): List<Policy> {
        val policies = ArrayList<Policy>()
        for (i in 1..count)
            policies.add(createPolicy(testPolicy()))
        return policies
    }

    private fun doSearch(params: String): NoPageResponse<Policy> {
        val response = createTemplate().exchange(policyUrl() + "/search${params}", HttpMethod.GET, null,
                object: ParameterizedTypeReference<NoPageResponse<Policy>>() {})
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertNotNull(response.body)
        return response.body!!
    }

    private fun doFilter(params: String): NoPageResponse<Policy> {
        val response = createTemplate().exchange(policyUrl() + "/${params}", HttpMethod.GET, null,
                object: ParameterizedTypeReference<NoPageResponse<Policy>>() {})
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertNotNull(response.body)
        return response.body!!
    }

    @Test
    fun `Filter all policies`() {
        createPolicies(5)
        val pages = doFilter("")
        Assertions.assertTrue(pages.size > 5, "Expected more than 5 policies to exist by default")
        Assertions.assertEquals(pages.size, pages.content.size, "The page size should be the content size")
    }

    @Test
    fun `Filter for resources`() {
        val policies = createPolicies(2).map{ it.id to it }.toMap()
        val ids = policies.values.map { it.resources!!.iterator().next() }.toHashSet()
        ids.add(rndResource())
        val pages = doFilter("?resources=${ids.joinToString(",")}")
        Assertions.assertEquals(policies.size, pages.content.size)
        for (policy in pages.content) {
            Assertions.assertTrue(policies.containsKey(policy.id))
            TestUtils.assertPoliciesEqual(policy, policies[policy.id] ?: error("Policy missing"))
        }
    }

    @Test
    fun `Filter for subjects`() {
        val policies = createPolicies(2).map{ it.id to it }.toMap()
        val ids = policies.values.map { it.subjects!!.iterator().next() }.toHashSet()
        ids.add(rndSubject())
        val pages = doFilter("?subjects=${ids.joinToString(",")}")
        Assertions.assertEquals(policies.size, pages.content.size)
        for (policy in pages.content) {
            Assertions.assertTrue(policies.containsKey(policy.id))
            TestUtils.assertPoliciesEqual(policy, policies[policy.id] ?: error("Policy missing"))
        }
    }

    @Test
    fun `Confirm resource-subject filter overlap`() {
        val p1 = createPolicy(testPolicy())
        val pages = doFilter("?subjects=${p1.subjects!!.iterator().next()}&resources=${p1.resources!!.iterator().next()}")
        Assertions.assertEquals(1, pages.content.size)
        TestUtils.assertPoliciesEqual(p1, pages.content[0])
    }

    @Test
    fun `Confirm resource-subject exclusive filter overlap`() {
        val p1 = createPolicy(testPolicy())
        val p2 = createPolicy(testPolicy())
        val pages = doFilter("?subjects=${p1.subjects!!.iterator().next()}&resources=${p2.resources!!.iterator().next()}")
        Assertions.assertEquals(0, pages.content.size)
    }

    @Test
    fun `Open policy search`() {
        createPolicies(5)
        val pages = doSearch("")
        Assertions.assertTrue(pages.size > 5, "Expected more than 5 policies to exist by default")
        Assertions.assertEquals(pages.size, pages.content.size, "The page size should be the content size")
    }

    @Test
    fun `Test policy search pagination`() {
        createPolicies(5)
        val pages = doSearch("?page=2&size=2")
        Assertions.assertEquals(2, pages.page, "Page of response should be the page specified in the request")
        Assertions.assertEquals(2, pages.size, "This page should (probably) be full")
        Assertions.assertTrue(pages.hasNext!!, "Expected more than 4 policies")
    }

    @Test
    fun `Search for policies`() {
        val policies = createPolicies(2).map{ it.id to it }.toMap()
        val ids = HashSet(policies.keys)
        ids.add(EntityUtils.uuid())
        val pages = doSearch("?policies=${ids.joinToString(",")}")
        Assertions.assertEquals(policies.size, pages.content.size)
        for (policy in pages.content) {
            Assertions.assertTrue(policies.containsKey(policy.id))
            TestUtils.assertPoliciesEqual(policy, policies[policy.id] ?: error("Policy missing"))
        }
    }

    @Test
    fun `Search for resources`() {
        val policies = createPolicies(2).map{ it.id to it }.toMap()
        val ids = policies.values.map { it.resources!!.iterator().next() }.toHashSet()
        ids.add(rndResource())
        val pages = doSearch("?resources=${ids.joinToString(",")}")
        Assertions.assertEquals(policies.size, pages.content.size)
        for (policy in pages.content) {
            Assertions.assertTrue(policies.containsKey(policy.id))
            TestUtils.assertPoliciesEqual(policy, policies[policy.id] ?: error("Policy missing"))
        }
    }

    @Test
    fun `Search for subjects`() {
        val policies = createPolicies(2).map{ it.id to it }.toMap()
        val ids = policies.values.map { it.subjects!!.iterator().next() }.toHashSet()
        ids.add(rndSubject())
        val pages = doSearch("?subjects=${ids.joinToString(",")}")
        Assertions.assertEquals(policies.size, pages.content.size)
        for (policy in pages.content) {
            Assertions.assertTrue(policies.containsKey(policy.id))
            TestUtils.assertPoliciesEqual(policy, policies[policy.id] ?: error("Policy missing"))
        }
    }

    @Test
    fun `Confirm resource-subject search overlap`() {
        val p1 = createPolicy(testPolicy())
        val p2 = createPolicy(testPolicy())
        val policies = mapOf(Pair(p1.id, p1), Pair(p2.id, p2))
        val pages = doSearch("?subjects=${p1.subjects!!.iterator().next()}&resources=${p2.resources!!.iterator().next()}")
        Assertions.assertEquals(policies.size, pages.content.size)
        for (policy in pages.content) {
            Assertions.assertTrue(policies.containsKey(policy.id))
            TestUtils.assertPoliciesEqual(policy, policies[policy.id] ?: error("Policy missing"))
        }
    }

    @Test
    fun `Confirm policy-resource search overlap`() {
        val p1 = createPolicy(testPolicy())
        val p2 = createPolicy(testPolicy())
        val policies = mapOf(Pair(p1.id, p1), Pair(p2.id, p2))
        val pages = doSearch("?policies=${p1.id}&resources=${p2.resources!!.iterator().next()}")
        Assertions.assertEquals(policies.size, pages.content.size)
        for (policy in pages.content) {
            Assertions.assertTrue(policies.containsKey(policy.id))
            TestUtils.assertPoliciesEqual(policy, policies[policy.id] ?: error("Policy missing"))
        }
    }

    @Test
    fun `Confirm policy-subject search overlap`() {
        val p1 = createPolicy(testPolicy())
        val p2 = createPolicy(testPolicy())
        val policies = mapOf(Pair(p1.id, p1), Pair(p2.id, p2))
        val pages = doSearch("?policies=${p1.id}&subjects=${p2.subjects!!.iterator().next()}")
        Assertions.assertEquals(policies.size, pages.content.size)
        for (policy in pages.content) {
            Assertions.assertTrue(policies.containsKey(policy.id))
            TestUtils.assertPoliciesEqual(policy, policies[policy.id] ?: error("Policy missing"))
        }
    }

    @Test
    fun `Confirm policy-status search overlap`() {
        val p1 = createPolicy(testPolicy())
        var p2 = testPolicy()
        p2.status = EntityStatus.INACTIVE
        p2 = createPolicy(p2)
        val ids = hashSetOf(p1.id, p2.id)
        val pages = doSearch("?policies=${ids.joinToString(",")}&status=${EntityStatus.INACTIVE.name}")
        Assertions.assertEquals(1, pages.content.size)
        Assertions.assertEquals(p2, pages.content[0])
    }

}
