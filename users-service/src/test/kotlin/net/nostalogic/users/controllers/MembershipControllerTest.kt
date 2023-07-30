package net.nostalogic.users.controllers

import net.nostalogic.config.DatabaseLoader
import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.NoEntity
import net.nostalogic.users.UsersApplication
import net.nostalogic.users.constants.GroupType
import net.nostalogic.users.constants.MembershipRole
import net.nostalogic.users.constants.MembershipStatus
import net.nostalogic.users.datamodel.memberships.GroupMembershipChanges
import net.nostalogic.users.datamodel.memberships.Membership
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test", "test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [UsersApplication::class])
class MembershipControllerTest(@Autowired dbLoader: DatabaseLoader): BaseControllerTest(dbLoader) {

    private val baseUserId = "f8680c40-2280-4125-812e-25dce05b4d"
    private val baseGroupId = "53941203-f159-4a2a-8cc8-5af32c0153"

    private fun mockReadPermissions() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, true))),
            Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, true)))))
    }

    private fun assertPageResponse(exchange: ResponseEntity<NoPageResponse<Membership>>, size: Int) {
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals(size, exchange.body!!.size)
        assertFalse(exchange.body!!.hasNext!!)
    }

    @Test
    fun `Confirm all memberships can be retrieved`() {
        mockReadPermissions()
        val exchange = exchange(
            entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<NoPageResponse<Membership>>() {},
            method = HttpMethod.GET, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}")
        assertPageResponse(exchange, 10)
    }

    @Test
    fun `Confirm memberships can be paged`() {
        mockReadPermissions()
        val exchange = exchange(
            entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<NoPageResponse<Membership>>() {},
            method = HttpMethod.GET, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}?size=5")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals(5, exchange.body!!.size)
        assertTrue(exchange.body!!.hasNext!!)
    }

    @Test
    fun `Confirm memberships are returned without permissions`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, false))),
            Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, false)))))
        val exchange = exchange(
            entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<NoPageResponse<Membership>>() {},
            method = HttpMethod.GET, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}")
        assertPageResponse(exchange, 0)
    }

    @Test
    fun `Confirm memberships can be filtered by user ID`() {
        mockReadPermissions()
        val exchange = exchange(
            entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<NoPageResponse<Membership>>() {},
            method = HttpMethod.GET, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}?userId=${baseUserId}01,${baseUserId}02,${baseUserId}03")
        assertPageResponse(exchange, 3)
    }

    @Test
    fun `Confirm memberships can be filtered by group ID`() {
        mockReadPermissions()
        val exchange = exchange(
            entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<NoPageResponse<Membership>>() {},
            method = HttpMethod.GET, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}?groupId=${baseGroupId}01,${baseGroupId}44")
        assertPageResponse(exchange, 1)
    }

    @Test
    fun `Confirm memberships can be filtered by group type`() {
        mockReadPermissions()
        val exchange = exchange(
            entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<NoPageResponse<Membership>>() {},
            method = HttpMethod.GET, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}?rights=true")
        assertPageResponse(exchange, 6)
    }

    @Test
    fun `Confirm memberships can be filtered by status`() {
        mockReadPermissions()
        val exchange = exchange(
            entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<NoPageResponse<Membership>>() {},
            method = HttpMethod.GET, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}?status=INVITED,APPLIED")
        assertPageResponse(exchange, 2)
    }

    @Test
    fun `Confirm multiple filters can be applied for memberships`() {
        mockReadPermissions()
        val exchange = exchange(
            entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<NoPageResponse<Membership>>() {},
            method = HttpMethod.GET, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}?status=INVITED,APPLIED")
        assertPageResponse(exchange, 2)
    }

    @Test
    fun `Get memberships for user`() {
        mockReadPermissions()
        val exchange = exchange(
            entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<NoPageResponse<Membership>>() {},
            method = HttpMethod.GET, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}${MembershipController.USERS_ENDPOINT}/${baseUserId}08")
        assertPageResponse(exchange, 1)
        val membership = exchange.body!!.content.first()
        assertNull(membership.userId)
        assertNull(membership.username)
        assertEquals("${baseGroupId}08", membership.groupId)
        assertEquals("Group 08", membership.group)
        assertEquals(MembershipRole.REGULAR, membership.role)
        assertEquals(MembershipStatus.INVITED, membership.status)
        assertEquals(GroupType.SYSTEM, membership.groupType)
        assertTrue(membership.rights!!)
    }

    @Test
    fun `Get memberships for group`() {
        mockReadPermissions()
        val exchange = exchange(
            entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<NoPageResponse<Membership>>() {},
            method = HttpMethod.GET, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}${MembershipController.GROUPS_ENDPOINT}/${baseGroupId}01")
        assertPageResponse(exchange, 1)
        val membership = exchange.body!!.content.first()
        assertNull(membership.group)
        assertNull(membership.groupType)
        assertNull(membership.groupId)
        assertEquals("${baseUserId}01", membership.userId)
        assertEquals("User 01", membership.username)
        assertEquals(MembershipStatus.ACTIVE, membership.status)
        assertEquals(MembershipRole.OWNER, membership.role)
    }

    @Test
    fun `Add users to group`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, true))),
            Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, true)))))
        val users = setOf("${baseUserId}02", "${baseUserId}03")
        val exchange = exchange(
            entity = HttpEntity(users, testHeaders()),
            responseType = object : ParameterizedTypeReference<List<GroupMembershipChanges>>() {},
            method = HttpMethod.PUT, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}${MembershipController.GROUPS_ENDPOINT}/${baseGroupId}01")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals(1, exchange.body!!.size)
        assertEquals(2, exchange.body!!.first().memberships.size)
        for (change in exchange.body!!.first().memberships) {
            assertNull(change.oldStatus)
            assertEquals(MembershipStatus.ACTIVE, change.newStatus)
        }
        val groupMembers = exchange(
            entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<NoPageResponse<Membership>>() {},
            method = HttpMethod.GET, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}${MembershipController.GROUPS_ENDPOINT}/${baseGroupId}01")
        assertPageResponse(groupMembers, 3)
    }

    @Test
    fun `Add user to groups`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, true))),
            Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, true)))))
        val groups = setOf("${baseGroupId}02", "${baseGroupId}03", "${baseGroupId}04")
        val exchange = exchange(
            entity = HttpEntity(groups, testHeaders()),
            responseType = object : ParameterizedTypeReference<List<GroupMembershipChanges>>() {},
            method = HttpMethod.PUT, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}${MembershipController.USERS_ENDPOINT}/${baseUserId}01")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals(3, exchange.body!!.size)
        for (groupChange in exchange.body!!) {
            assertEquals(1, groupChange.memberships.size)
            assertNull(groupChange.memberships.first().oldStatus)
            assertEquals(MembershipStatus.ACTIVE, groupChange.memberships.first().newStatus)
        }
        val userMemberships = exchange(
            entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<NoPageResponse<Membership>>() {},
            method = HttpMethod.GET, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}${MembershipController.USERS_ENDPOINT}/${baseUserId}01")
        assertPageResponse(userMemberships, 4)
    }

    @Test
    fun `Remove user from group`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, true))),
            Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, true)))))
        val users = setOf("${baseUserId}07")
        val exchange = exchange(
            entity = HttpEntity(users, testHeaders()),
            responseType = object : ParameterizedTypeReference<List<GroupMembershipChanges>>() {},
            method = HttpMethod.DELETE, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}${MembershipController.GROUPS_ENDPOINT}/${baseGroupId}07")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals(1, exchange.body!!.size)
        assertEquals(1, exchange.body!!.first().memberships.size)
        for (change in exchange.body!!.first().memberships) {
            assertTrue(change.changed)
            assertEquals(MembershipStatus.ACTIVE, change.oldStatus)
            assertNull(change.newStatus)
        }
        val groupMembers = exchange(
            entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<NoPageResponse<Membership>>() {},
            method = HttpMethod.GET, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}${MembershipController.GROUPS_ENDPOINT}/${baseGroupId}06")
        assertPageResponse(groupMembers, 0)
    }

    @Test
    fun `Remove un-removable users from group`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, true))),
            Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, true)))))
        val users = setOf("${baseUserId}01", "${baseUserId}02")
        val exchange = exchange(
            entity = HttpEntity(users, testHeaders()),
            responseType = object : ParameterizedTypeReference<List<GroupMembershipChanges>>() {},
            method = HttpMethod.DELETE, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}${MembershipController.GROUPS_ENDPOINT}/${baseGroupId}01")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals(1, exchange.body!!.size)
        assertEquals(2, exchange.body!!.first().memberships.size)
        for (change in exchange.body!!.first().memberships) {
            assertFalse(change.changed)
            Assertions.assertNotNull(change.failReason)
        }
    }

    @Test
    fun `Remove user from groups`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, true))),
            Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, true)))))
        val groups = setOf("${baseGroupId}07")
        val exchange = exchange(
            entity = HttpEntity(groups, testHeaders()),
            responseType = object : ParameterizedTypeReference<List<GroupMembershipChanges>>() {},
            method = HttpMethod.DELETE, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}${MembershipController.USERS_ENDPOINT}/${baseUserId}07")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals(1, exchange.body!!.size)
        assertEquals(1, exchange.body!!.first().memberships.size)
        for (change in exchange.body!!.first().memberships) {
            assertTrue(change.changed)
            assertEquals("${baseUserId}07", change.userId)
            assertEquals(MembershipStatus.ACTIVE, change.oldStatus)
            assertNull(change.newStatus)
        }
        val groupMembers = exchange(
            entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<NoPageResponse<Membership>>() {},
            method = HttpMethod.GET, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}${MembershipController.USERS_ENDPOINT}/${baseUserId}07")
        assertPageResponse(groupMembers, 0)
    }

    @Test
    fun `Change user role and status`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, true))),
            Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, true)))))
        val membership = Membership(status = MembershipStatus.ACTIVE, role = MembershipRole.OWNER)
        val exchange = exchange(
            entity = HttpEntity(membership, testHeaders()),
            responseType = object : ParameterizedTypeReference<Membership>() {},
            method = HttpMethod.PUT, url = "$baseApiUrl${MembershipController.MEMBERS_ENDPOINT}${MembershipController.GROUPS_ENDPOINT}/${baseGroupId}09" +
                    "${MembershipController.USERS_ENDPOINT}/${baseUserId}09")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        val updated = exchange.body!!
        assertEquals(MembershipStatus.ACTIVE, updated.status)
        assertEquals(MembershipRole.OWNER, updated.role)
    }

}
