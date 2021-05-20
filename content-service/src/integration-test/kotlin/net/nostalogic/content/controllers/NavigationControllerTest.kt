package net.nostalogic.content.controllers

import net.nostalogic.config.DatabaseLoader
import net.nostalogic.content.ContentApplication
import net.nostalogic.content.config.ContentLoader
import net.nostalogic.content.datamodel.navigations.Nav
import net.nostalogic.content.datamodel.navigations.NavDetails
import net.nostalogic.datamodel.ErrorResponse
import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
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
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [ContentApplication::class])
class NavigationControllerTest(@Autowired dbLoader: DatabaseLoader, @Autowired contentLoader: ContentLoader): BaseControllerTest(dbLoader, contentLoader) {

    private val aboutPageId = "d7318720-e807-409a-99c5-2ae2e7817289"
    private val roadmapPageId = "87927756-5aa5-440c-80dd-394e2992fe65"
    private val changelogPageId = "2e1add8c-e6e9-4583-9ac0-45176aae205a"

    private fun getPathNav(path: String): NavDetails {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.READ, true)))))
        val response = exchange(entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<NavDetails>() {},
            method = HttpMethod.GET,
            url = "$baseApiUrl${NavigationController.NAV_ENDPOINT}/path/${path}")
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertNotNull(response.body)
        return response.body!!
    }

    @Test
    fun `Get base nav`() {
        val details = getPathNav("")
        Assertions.assertEquals(0, details.breadcrumbs.size)
        Assertions.assertEquals("", details.fullPath)
        Assertions.assertEquals("", details.urn)
        Assertions.assertEquals(0, details.topLinks.size)
        Assertions.assertEquals(3, details.sideLinks.size)
    }

    @Test
    fun `Get about nav`() {
        val details = getPathNav("about")
        Assertions.assertEquals(1, details.breadcrumbs.size)
        Assertions.assertEquals(details.breadcrumbs[0], "about")
        Assertions.assertEquals("about", details.fullPath)
        Assertions.assertEquals("about", details.urn)
        Assertions.assertEquals(0, details.topLinks.size)
        Assertions.assertEquals(2, details.sideLinks.size)
    }

    @Test
    fun `Get about changelog nav`() {
        val details = getPathNav("about/changelog")
        Assertions.assertEquals(2, details.breadcrumbs.size)
        Assertions.assertEquals(details.breadcrumbs[0], "about")
        Assertions.assertEquals(details.breadcrumbs[1], "changelog")
        Assertions.assertEquals("about/changelog", details.fullPath)
        Assertions.assertEquals("changelog", details.urn)
        Assertions.assertEquals(0, details.topLinks.size)
        Assertions.assertEquals(0, details.sideLinks.size)
    }

    @Test
    fun `Get nonexistent nav`() {
        val response = exchangeError(entity = HttpEntity<Unit>(testHeaders()), method = HttpMethod.GET,
            url = "$baseApiUrl${NavigationController.NAV_ENDPOINT}/path/nonexistent")
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals(404, response.body!!.status)
        Assertions.assertEquals(503001, response.body!!.errorCode)
    }

    private fun createNavWithPermissions(blueprint: Nav): Nav {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.CREATE, true)))))
        val response = exchange(entity = HttpEntity<Nav>(blueprint, testHeaders()),
            responseType = object : ParameterizedTypeReference<Nav> () {}, method = HttpMethod.POST,
            url = "$baseApiUrl${NavigationController.NAV_ENDPOINT}/navigations")
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertNotNull(response.body)
        return response.body!!
    }

    private fun createNavWithFail(blueprint: Nav): ResponseEntity<ErrorResponse> {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.CREATE, true)))))
        return exchangeError(entity = HttpEntity<Nav>(blueprint, testHeaders()), method = HttpMethod.POST,
            url = "$baseApiUrl${NavigationController.NAV_ENDPOINT}/navigations")
    }

    @Test
    fun `Create a bottom level navigation`() {
        val blueprint = Nav(text = "Some text", icon = "An icon", path = "generic")
        val nav = createNavWithPermissions(blueprint)
        Assertions.assertEquals(blueprint.icon, nav.icon)
        Assertions.assertEquals(blueprint.text, nav.text)
        Assertions.assertEquals(blueprint.path, nav.path)
        Assertions.assertEquals(EntityStatus.INACTIVE, nav.status)
        Assertions.assertNull(nav.container)
        Assertions.assertNull(nav.parentId)
        Assertions.assertNotNull(nav.id)
    }

    @Test
    fun `Create a sub level navigation`() {
        val blueprint = Nav(text = "Other text", icon = "Another icon", path = "about/changelog/subpath", parentId = "2e1add8c-e6e9-4583-9ac0-45176aae205a")
        val nav = createNavWithPermissions(blueprint)
        Assertions.assertEquals(blueprint.parentId, nav.parentId)
        Assertions.assertEquals(blueprint.path, nav.path)
        Assertions.assertNotNull(nav.id)
    }

    @Test
    fun `Sub level navigation full paths can be inferred`() {
        val blueprint = Nav(text = "Other text", icon = "Another icon", path = "subpath", parentId = "2e1add8c-e6e9-4583-9ac0-45176aae205a")
        val nav = createNavWithPermissions(blueprint)
        Assertions.assertEquals(blueprint.parentId, nav.parentId)
        Assertions.assertEquals("about/changelog/${blueprint.path}", nav.path)
        Assertions.assertNotNull(nav.id)
    }

    @Test
    fun `Creating a sub level navigation requires a parent ID`() {
        val blueprint = Nav(text = "Other text", icon = "Another icon", path = "about/changelog/subpath")
        val response = createNavWithFail(blueprint)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals(507003, response.body!!.errorCode)
    }

    @Test
    fun `Creating a navigation with a duplicate path should fail`() {
        val blueprint = Nav(text = "Other text", icon = "Another icon", path = "about/changelog", parentId = "d7318720-e807-409a-99c5-2ae2e7817289")
        val response = createNavWithFail(blueprint)
        Assertions.assertEquals(HttpStatus.CONFLICT, response.statusCode)
        Assertions.assertEquals(505003, response.body!!.errorCode)
    }

    @Test
    fun `Creating a navigation with a nonexistent parent should fail`() {
        val blueprint = Nav(text = "Other text", icon = "Another icon", path = "something", parentId = EntityUtils.uuid())
        val response = createNavWithFail(blueprint)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals(504008, response.body!!.errorCode)
    }

    @Test
    fun `Basic navigation deletion`() {
        val nav = createNavWithPermissions(Nav(text = "Other text", icon = "Another icon", path = "something"))
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.DELETE, true)))))
        val deleteResponse = exchange(entity = HttpEntity<Unit>(testHeaders()), method = HttpMethod.DELETE,
            responseType = object : ParameterizedTypeReference<Unit> () {},
            url = "$baseApiUrl${NavigationController.NAV_ENDPOINT}/navigations/${nav.id}")
        Assertions.assertEquals(HttpStatus.OK, deleteResponse.statusCode)
        val secondDelete = exchangeError(entity = HttpEntity<Unit>(testHeaders()), method = HttpMethod.DELETE,
            url = "$baseApiUrl${NavigationController.NAV_ENDPOINT}/navigations/${nav.id}")
        Assertions.assertEquals(HttpStatus.NOT_FOUND, secondDelete.statusCode)
        Assertions.assertEquals(504006, secondDelete.body!!.errorCode)
    }

    @Test
    fun `Deleting a parent navigation should also delete its child navigations`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.DELETE, true)))))
        val deleteResponse = exchange(entity = HttpEntity<Unit>(testHeaders()), method = HttpMethod.DELETE,
            responseType = object : ParameterizedTypeReference<Unit> () {},
            url = "$baseApiUrl${NavigationController.NAV_ENDPOINT}/navigations/$aboutPageId")
        Assertions.assertEquals(HttpStatus.OK, deleteResponse.statusCode)
        val childOne = exchangeError(entity = HttpEntity<Unit>(testHeaders()), method = HttpMethod.DELETE,
            url = "$baseApiUrl${NavigationController.NAV_ENDPOINT}/navigations/$roadmapPageId")
        Assertions.assertEquals(HttpStatus.NOT_FOUND, childOne.statusCode)
        val childTwo = exchangeError(entity = HttpEntity<Unit>(testHeaders()), method = HttpMethod.DELETE,
            url = "$baseApiUrl${NavigationController.NAV_ENDPOINT}/navigations/$changelogPageId")
        Assertions.assertEquals(HttpStatus.NOT_FOUND, childTwo.statusCode)
    }

    @Test
    fun `Get a navigation by ID`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.READ, true)))))
        val response = exchange(entity = HttpEntity<Unit>(testHeaders()), method = HttpMethod.GET,
            responseType = object : ParameterizedTypeReference<Nav> () {},
            url = "$baseApiUrl${NavigationController.NAV_ENDPOINT}/navigations/$aboutPageId")
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertNotNull(response.body)
        val nav = response.body!!
        Assertions.assertEquals(aboutPageId, nav.id)
        Assertions.assertEquals("About", nav.text)
        Assertions.assertEquals("info", nav.icon)
        Assertions.assertEquals("about", nav.path)
        Assertions.assertEquals(EntityStatus.ACTIVE, nav.status)
    }

    @Test
    fun `Get a nonexistent navigation by ID`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.READ, true)))))
        val response = exchangeError(entity = HttpEntity<Unit>(testHeaders()), method = HttpMethod.GET,
            url = "$baseApiUrl${NavigationController.NAV_ENDPOINT}/navigations/${EntityUtils.uuid()}")
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals(504005, response.body!!.errorCode)
    }

    private fun editNav(id: String, update: Nav): Nav {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.EDIT, true)))))
        val response = exchange(entity = HttpEntity<Nav>(update, testHeaders()), method = HttpMethod.PUT,
            responseType = object : ParameterizedTypeReference<Nav> () {},
            url = "$baseApiUrl${NavigationController.NAV_ENDPOINT}/navigations/$id")
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertNotNull(response.body)
        return response.body!!
    }

    @Test
    fun `Edit a navigation`() {
        val edit = Nav(text = "Changed Text", icon = "OtherIcon", status = EntityStatus.INACTIVE, path = null)
        val edited = editNav(aboutPageId, edit)
        Assertions.assertEquals(edit.text, edited.text)
        Assertions.assertEquals(edit.icon, edited.icon)
        Assertions.assertEquals("about", edited.path)
        Assertions.assertEquals(edit.status, edited.status)
    }

    @Test
    fun `Editing a parent navigation path should change the children`() {
        val edit = Nav(path = "changed")
        val edited = editNav(aboutPageId, edit)
        Assertions.assertEquals(edit.path, edited.path)
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.READ, true)))))
        val changelogPage = exchange(entity = HttpEntity<Unit>(testHeaders()), method = HttpMethod.GET,
            responseType = object : ParameterizedTypeReference<Nav> () {},
            url = "$baseApiUrl${NavigationController.NAV_ENDPOINT}/navigations/$changelogPageId")
        Assertions.assertEquals("changed/changelog", changelogPage.body!!.path)
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.READ, true)))))
        val roadmapPage = exchange(entity = HttpEntity<Unit>(testHeaders()), method = HttpMethod.GET,
            responseType = object : ParameterizedTypeReference<Nav> () {},
            url = "$baseApiUrl${NavigationController.NAV_ENDPOINT}/navigations/$roadmapPageId")
        Assertions.assertEquals("changed/roadmap", roadmapPage.body!!.path)
    }

    @Test
    fun `Null or empty navigation fields are ignored`() {
        val edit = Nav(path = "differ", status = null)
        val edited = editNav(changelogPageId, edit)
        Assertions.assertEquals("Changelog", edited.text)
        Assertions.assertEquals("change_history", edited.icon)
        Assertions.assertEquals("about/differ", edited.path)
        Assertions.assertEquals(EntityStatus.ACTIVE, edited.status)
    }

    @Test
    fun `Editing a nonexistent navigation should throw an error`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.EDIT, true)))))
        val response = exchangeError(entity = HttpEntity<Nav>(Nav(text = "nochange"), testHeaders()), method = HttpMethod.PUT,
            url = "$baseApiUrl${NavigationController.NAV_ENDPOINT}/navigations/${EntityUtils.uuid()}")
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals(504007, response.body!!.errorCode)
    }

    private fun doSearch(criteria: String): NoPageResponse<Nav> {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.READ, true)))))
        val response = exchange(entity = HttpEntity<Unit>(testHeaders()), method = HttpMethod.GET,
            responseType = object : ParameterizedTypeReference<NoPageResponse<Nav>> () {},
            url = "$baseApiUrl${NavigationController.NAV_ENDPOINT}/navigations?$criteria")
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertNotNull(response.body)
        return response.body!!
    }

    @Test
    fun `Searching by ID should return the specified navs`() {
        val response = doSearch("id=$aboutPageId,$changelogPageId,$roadmapPageId,${EntityUtils.uuid()}")
        Assertions.assertEquals(3, response.content.size)
        Assertions.assertEquals(3, response.size)
    }

    @Test
    fun `Searching by urns should return navs containing those`() {
        val response = doSearch("urn=changelog,roadmap")
        Assertions.assertEquals(2, response.content.size)
        Assertions.assertEquals(2, response.size)
        Assertions.assertEquals(roadmapPageId, response.content[0].id)
        Assertions.assertEquals(changelogPageId, response.content[1].id)
    }

    @Test
    fun `Searching by urn should return children containing the same specified part`() {
        val response = doSearch("urn=about")
        Assertions.assertEquals(3, response.content.size)
        Assertions.assertEquals(3, response.size)
        Assertions.assertEquals(aboutPageId, response.content[0].id)
        Assertions.assertEquals(roadmapPageId, response.content[1].id)
        Assertions.assertEquals(changelogPageId, response.content[2].id)
    }

    @Test
    fun `Search parameters should be case insensitive`() {
        val response = doSearch("text=ABOUT&urn=chAnGeLog")
        Assertions.assertEquals(2, response.content.size)
        Assertions.assertEquals(2, response.size)
    }

    @Test
    fun `Unmatched search fields should be ignored`() {
        val response = doSearch("urn=nothing&text=about&id=$changelogPageId")
        Assertions.assertEquals(2, response.content.size)
        Assertions.assertEquals(2, response.size)
    }

    @Test
    fun `Search results should be filtered by status if specified`() {
        val response = doSearch("id=$aboutPageId&status=${EntityStatus.INACTIVE.name}")
        Assertions.assertEquals(0, response.content.size)
        Assertions.assertEquals(0, response.size)
    }

    @Test
    fun `All navs should be returned if no criteria are specified`() {
        val response = doSearch("")
        Assertions.assertTrue(response.content.size > 5)
        Assertions.assertTrue(response.size > 5)
    }

    @Test
    fun `Confirm size specification of search results`() {
        val response = doSearch("size=2")
        Assertions.assertEquals(2, response.content.size)
        Assertions.assertEquals(2, response.size)
        Assertions.assertTrue(response.totalSize > 5)
    }

    @Test
    fun `Confirm pagination of search results`() {
        val response = doSearch("size=2&page=2")
        Assertions.assertEquals(2, response.page)
        Assertions.assertEquals(2, response.size)
        Assertions.assertTrue(response.totalSize > 5)
    }

}
