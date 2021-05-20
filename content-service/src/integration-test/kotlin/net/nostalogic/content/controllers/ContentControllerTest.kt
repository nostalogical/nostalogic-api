package net.nostalogic.content.controllers

import net.nostalogic.config.DatabaseLoader
import net.nostalogic.content.ContentApplication
import net.nostalogic.content.config.ContentLoader
import net.nostalogic.content.datamodel.ContainerType
import net.nostalogic.content.datamodel.Content
import net.nostalogic.content.datamodel.articles.Article
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntityStatus
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [ContentApplication::class])
class ContentControllerTest(@Autowired dbLoader: DatabaseLoader, @Autowired contentLoader: ContentLoader): BaseControllerTest(dbLoader, contentLoader) {

    private fun getArticleContent(path: String): Content<Article> {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.READ, true)))))
        return exchange(
            entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<Content<Article>>() {},
            method = HttpMethod.GET, url = "$baseApiUrl${ContentController.CONTENT_ENDPOINT}/content/$path").body!!
    }

    @Test
    fun `Get 'about' page content`() {
        val content = getArticleContent("about")
        Assertions.assertNotNull(content)
        Assertions.assertEquals(ContainerType.ARTICLE, content.type)
        Assertions.assertTrue(content.sideLinks.size > 1)
        Assertions.assertEquals(1, content.breadcrumbs.size)
        Assertions.assertEquals(content.breadcrumbs[0], "about")
        val article = content.content
        Assertions.assertTrue(article.body!!.startsWith("##  The site"))
        Assertions.assertEquals(EntityStatus.ACTIVE, article.status)
        Assertions.assertEquals("54176cc9-c348-4240-ba5e-0d23affc6bd6", article.id)
        Assertions.assertEquals("About", article.title)
        Assertions.assertNull(article.revisionStatus)
    }

    @Test
    fun `Base page is a system path and has no content`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.NAV, hashMapOf(Pair(PolicyAction.READ, true)))))
        val content = exchangeError(entity = HttpEntity<Unit>(testHeaders()), method = HttpMethod.GET,
            url = "$baseApiUrl${ContentController.CONTENT_ENDPOINT}/content/")
        Assertions.assertNotNull(content)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, content.statusCode)
        Assertions.assertEquals(404, content.body!!.status)
        Assertions.assertEquals(503002, content.body!!.errorCode)
        Assertions.assertTrue(content.body!!.debugMessage!!.contains("system path"),
            "If a path is marked as a system path it's 'reserved' for the client. No content can be stored here so this method should return a 404, " +
                    "but it may still have associated CMS links so it still has a nav and this unique error message.")
    }

    @Test
    fun `Get 'about changelog' page content`() {
        val content = getArticleContent("about/changelog")
        Assertions.assertNotNull(content)
        Assertions.assertEquals(2, content.breadcrumbs.size)
        Assertions.assertEquals(content.breadcrumbs[0], "about")
        Assertions.assertEquals(content.breadcrumbs[1], "changelog")
        val article = content.content
        Assertions.assertEquals(EntityStatus.ACTIVE, article.status)
        Assertions.assertEquals("6b78270f-b3e6-4bca-9bca-cc262589cdc8", article.id)
        Assertions.assertEquals("Changelog", article.title)
        Assertions.assertNull(article.revisionStatus)
    }

    @Test
    fun `Redundant slashes are ignored`() {
        val content = getArticleContent("//about//////changelog//")
        Assertions.assertNotNull(content)
        Assertions.assertEquals(2, content.breadcrumbs.size)
        Assertions.assertEquals(content.breadcrumbs[0], "about")
        Assertions.assertEquals(content.breadcrumbs[1], "changelog")
        Assertions.assertNotNull(content.content)
    }

}
