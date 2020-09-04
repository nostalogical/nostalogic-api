package net.nostalogic.access.controllers

import net.nostalogic.access.AccessApplication
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
import kotlin.collections.HashSet

@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [AccessApplication::class])
class AccessControllerTest(@Autowired dbLoader: DatabaseLoader) : BaseControllerTest(dbLoader) {



    private fun accessUrl(): String {
        return baseApiUrl + AccessController.ACCESS_ENDPOINT
    }

    private fun policyUrl(): String {
        return accessUrl() + AccessController.POLICIES_URI
    }

    private fun testPolicy(): Policy {
        return Policy(
                name = "Test Policy",
                priority = PolicyPriority.LEVEL_TWO,
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT_OWN, true)),
                resources = hashSetOf(rndResource()),
                subjects = hashSetOf(rndSubject())
        )
    }

    private fun createPolicy(policy: Policy): Policy {
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

    private fun assertPoliciesEqual(p1: Policy, p2:Policy, includeIds: Boolean = true) {
        if (includeIds)
            Assertions.assertEquals(p1, p2)
        else {
            Assertions.assertEquals(p1.name, p2.name)
            Assertions.assertEquals(p1.status, p2.status)
            Assertions.assertEquals(p1.priority, p2.priority)
            Assertions.assertEquals(p1.permissions, p2.permissions)
            Assertions.assertEquals(p1.resources, p2.resources)
            Assertions.assertEquals(p1.subjects, p2.subjects)
        }
    }

    @Test
    fun `Create a policy`() {
        val policy = testPolicy()
        val response = createPolicy(policy)
        assertPoliciesEqual(policy, response, false)
    }

    @Test
    fun `Create a policy with empty lists`() {
        val policy = Policy(name = "Test blank", priority = PolicyPriority.LEVEL_ONE)
        policy.permissions = EnumMap(PolicyAction::class.java)
        policy.resources = HashSet()
        policy.subjects = HashSet()
        val response = createPolicy(policy)
        assertPoliciesEqual(policy, response, false)
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
        assertPoliciesEqual(policy, response.body!!)
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
        assertPoliciesEqual(policy, response.body!!)
    }

    @Test
    fun `Edit a policy with an invalid field`() {
        val policy = createPolicy(testPolicy())
        policy.name = "A long name over fifty character limit which should throw an exception"
        val response = exchange(entity = HttpEntity(policy),
                responseType = object: ParameterizedTypeReference<ErrorResponse> () {},
                method = HttpMethod.PUT, url = policyUrl() + "/${policy.id}")
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertTrue(response.body!!.debugMessage.contains("missing or invalid:\nname"))
    }

    private fun assertPolicyUpdate(policy: Policy) {
        val update = exchange(entity = HttpEntity(policy),
                responseType = object: ParameterizedTypeReference<Policy> () {},
                method = HttpMethod.PUT, url = policyUrl() + "/${policy.id}")
        Assertions.assertEquals(HttpStatus.OK, update.statusCode)
        Assertions.assertNotNull(update.body)
        assertPoliciesEqual(policy, update.body!!)
        val retrieve = createTemplate().exchange(policyUrl() + "/${policy.id}", HttpMethod.GET, null, Policy::class.java)
        assertPoliciesEqual(policy, retrieve.body!!)
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
        policy.priority = PolicyPriority.LEVEL_THREE
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

    private fun createPolicies(count: Int) {
        for (i in 1..count)
            createPolicy(testPolicy())
    }

    @Test
    fun `Open policy search`() {
        createPolicies(5)
        val response = createTemplate().exchange(policyUrl() + "/search", HttpMethod.GET, null,
                object: ParameterizedTypeReference<NoPageResponse<Policy>>() {})
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertTrue(response.body!!.size > 5, "Expected more than 5 policies to exist by default")
        Assertions.assertEquals(response.body!!.size, response.body!!.content.size, "The page size should be the content size")
    }

    @Test
    fun `Test policy search pagination`() {
        createPolicies(5)
        val response = createTemplate().exchange(policyUrl() + "/search?page=2&size=2", HttpMethod.GET, null,
                object: ParameterizedTypeReference<NoPageResponse<Policy>>() {})
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(2, response.body!!.page, "Page of response should be the page specified in the request")
        Assertions.assertEquals(2, response.body!!.size, "This page should (probably) be full")
        Assertions.assertTrue(response.body!!.hasNext!!, "Expected more than 4 policies")
    }

    // Do a bunch of searches with different criteria
    // Status, id, name, priority, resources, subjects, permissions
    // Policies, subjects, resources, and status can be searched by
    // For IDs, can search by a few and single IDs -


}
