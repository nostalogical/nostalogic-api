package net.nostalogic.users.controllers

import net.nostalogic.config.DatabaseLoader
import net.nostalogic.datamodel.ErrorResponse
import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.users.UsersApplication
import net.nostalogic.users.datamodel.groups.Group
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

@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [UsersApplication::class])
class GroupControllerTest(@Autowired dbLoader: DatabaseLoader): BaseControllerTest(dbLoader) {

    private val siteOwnerGroupId = "309e6617-2e5a-4d77-b51d-18097383233f"
    private val siteAdminGroupId = "06cd7155-576e-465d-8722-3eb8373351b7"

    private val testGroupName = "Test Group"
    private val testGroupDesc = "Description of the test group"

    private fun getGroup(id: String): Group {
        return exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<Group>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}/${id}").body!!
    }

    @Test
    fun `Get a group`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, true)))))
        val group = getGroup(siteOwnerGroupId)
        Assertions.assertNotNull(group)
        Assertions.assertEquals(siteOwnerGroupId, group.id)
        Assertions.assertEquals("Site Owner", group.name)
    }

    @Test
    fun `Get a group without permissions`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, false)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}/${siteOwnerGroupId}")
        Assertions.assertNotNull(exchange)
        Assertions.assertEquals(HttpStatus.FORBIDDEN, exchange.statusCode)
        Assertions.assertEquals(301007, exchange.body!!.errorCode)
    }

    @Test
    fun `Get a non-existent group`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}/${EntityUtils.uuid()}")
        Assertions.assertNotNull(exchange)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, exchange.statusCode)
        Assertions.assertEquals(304009, exchange.body!!.errorCode)
    }

    @Test
    fun `Get all groups with full permissions`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<NoPageResponse<Group>>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}").body!!
        Assertions.assertTrue(exchange.size > 1)
        Assertions.assertEquals(1, exchange.page)
        val group = exchange.content.iterator().next()
        Assertions.assertEquals(EntityStatus.ACTIVE, group.status)
    }

    @Test
    fun `Get all groups with permission for only one`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, false)))),
                resourcePermissions = hashMapOf(Pair(EntityReference(siteOwnerGroupId, NoEntity.GROUP).toString(),
                        hashMapOf(Pair(PolicyAction.READ, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<NoPageResponse<Group>>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}").body!!
        Assertions.assertEquals(1, exchange.size)
        Assertions.assertEquals(1, exchange.page)
        val group = exchange.content.iterator().next()
        Assertions.assertEquals(EntityStatus.ACTIVE, group.status)
        Assertions.assertEquals(siteOwnerGroupId, group.id)
    }

    @Test
    fun `Get groups filtered by IDs with full permissions`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<NoPageResponse<Group>>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}" +
                "?id=${siteOwnerGroupId},${siteAdminGroupId}").body!!
        Assertions.assertEquals(2, exchange.size)
        Assertions.assertEquals(1, exchange.page)
    }

    @Test
    fun `Get groups filtered by IDs with mixed permissions`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, false)))),
                resourcePermissions = hashMapOf(Pair(EntityReference(siteOwnerGroupId, NoEntity.GROUP).toString(),
                        hashMapOf(Pair(PolicyAction.READ, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<NoPageResponse<Group>>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}" +
                "?id=${siteOwnerGroupId},${siteAdminGroupId}").body!!
        Assertions.assertEquals(1, exchange.size)
        Assertions.assertEquals(1, exchange.page)
        val group = exchange.content.iterator().next()
        Assertions.assertEquals(siteOwnerGroupId, group.id)
    }

    private fun testGroup(): Group {
        val group = Group(name = testGroupName, description = testGroupDesc)
        val exchange = exchange(
                entity = HttpEntity(group),
                responseType = object : ParameterizedTypeReference<Group>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}")
        Assertions.assertEquals(HttpStatus.CREATED, exchange.statusCode)
        Assertions.assertNotNull(exchange.body)
        return exchange.body!!
    }

    @Test
    fun `Create a group with permission`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.CREATE, true)))))
        val group = testGroup()
        Assertions.assertEquals(testGroupName, group.name)
        Assertions.assertEquals(testGroupDesc, group.description)
        Assertions.assertNotNull(group.id)
    }

    @Test
    fun `Create a group without permission`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.CREATE, false)))))
        val exchange = exchange(
                entity = HttpEntity(Group(name = testGroupName, description = testGroupDesc)),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}")
        Assertions.assertEquals(HttpStatus.FORBIDDEN, exchange.statusCode)
        Assertions.assertEquals(301009, exchange.body!!.errorCode)
    }

    @Test
    fun `Create a group with an existing name`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.CREATE, true)))))
        val exchange = exchange(
                entity = HttpEntity(Group(name = "Site Owner", description = testGroupDesc)),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}")
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, exchange.statusCode)
        Assertions.assertEquals(307006, exchange.body!!.errorCode)
    }

    @Test
    fun `Create a group without a name`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.CREATE, true)))))
        val exchange = exchange(
                entity = HttpEntity(Group(name = "", description = testGroupDesc)),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}")
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, exchange.statusCode)
        Assertions.assertEquals(307006, exchange.body!!.errorCode)
    }

    @Test
    fun `Update a group`() {
        val newName = "Name change"
        val newDesc = "Different description"
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.EDIT, true), Pair(PolicyAction.READ, true)))))
        val exchange = exchange(
                entity = HttpEntity(Group(name = newName, description = newDesc)),
                responseType = object : ParameterizedTypeReference<Group>() {},
                method = HttpMethod.PUT, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}/${siteAdminGroupId}")
        Assertions.assertNotNull(exchange.body)
        Assertions.assertEquals(newName, exchange.body!!.name)
        Assertions.assertEquals(newDesc, exchange.body!!.description)
        val updated = getGroup(siteAdminGroupId)
        Assertions.assertEquals(newName, updated.name)
        Assertions.assertEquals(newDesc, updated.description)
    }

    @Test
    fun `Update a group without permissions`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.EDIT, false)))))
        val exchange = exchange(
                entity = HttpEntity(Group(name = "Name change", description = "Different description")),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.PUT, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}/${siteAdminGroupId}")
        Assertions.assertNotNull(exchange.body)
        Assertions.assertEquals(HttpStatus.FORBIDDEN, exchange.statusCode)
        Assertions.assertEquals(301010, exchange.body!!.errorCode)
    }

    @Test
    fun `Updating a group ignores empty fields`() {
        val newDesc = "Different description"
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.EDIT, true)))))
        val exchange = exchange(
                entity = HttpEntity(Group(description = newDesc)),
                responseType = object : ParameterizedTypeReference<Group>() {},
                method = HttpMethod.PUT, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}/${siteAdminGroupId}")
        Assertions.assertEquals("Admin", exchange.body!!.name)
        Assertions.assertEquals(newDesc, exchange.body!!.description)
    }

    @Test
    fun `Delete a group`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.DELETE, true), Pair(PolicyAction.READ, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<Group>() {},
                method = HttpMethod.DELETE, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}/${siteAdminGroupId}")
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertEquals(EntityStatus.DELETED, exchange.body!!.status)
        val deleted = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}/${siteAdminGroupId}")
        Assertions.assertEquals(HttpStatus.NOT_FOUND, deleted.statusCode)
        Assertions.assertEquals(304011, deleted.body!!.errorCode)
    }

    @Test
    fun `Delete a group without permissions`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.DELETE, false)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.DELETE, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}/${siteAdminGroupId}")
        Assertions.assertNotNull(exchange.body)
        Assertions.assertEquals(HttpStatus.FORBIDDEN, exchange.statusCode)
        Assertions.assertEquals(301008, exchange.body!!.errorCode)
    }

    @Test
    fun `Delete a non-existent group`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.DELETE, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.DELETE, url = "$baseApiUrl${GroupController.GROUPS_ENDPOINT}/${EntityUtils.uuid()}")
        Assertions.assertNotNull(exchange.body)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, exchange.statusCode)
        Assertions.assertEquals(304013, exchange.body!!.errorCode)
    }

}
