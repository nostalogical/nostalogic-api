package net.nostalogic.content.controllers

import net.nostalogic.config.DatabaseLoader
import net.nostalogic.content.ContentApplication
import net.nostalogic.content.config.ContentLoader
import net.nostalogic.content.datamodel.articles.Article
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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [ContentApplication::class])
class ArticleControllerTest(@Autowired dbLoader: DatabaseLoader, @Autowired contentLoader: ContentLoader): BaseControllerTest(dbLoader, contentLoader) {

    private val aboutArticleId = "54176cc9-c348-4240-ba5e-0d23affc6bd6"
    private val roadmapArticleId = "a7a47cc3-6542-4970-ad81-5bb37a6c2f6a"
    private val changelogArticleId = "6b78270f-b3e6-4bca-9bca-cc262589cdc8"

    private fun getArticle(id: String): Article {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.ARTICLE, hashMapOf(Pair(PolicyAction.READ, true)))))
        val response = exchange(entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<Article>() {},
            method = HttpMethod.GET,
            url = "$baseApiUrl${ArticleController.ARTICLE_ENDPOINT}/$id")
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertNotNull(response.body)
        return response.body!!
    }

    @Test
    fun `Get an article`() {
        val article = getArticle(aboutArticleId)
        Assertions.assertEquals(aboutArticleId, article.id)
        Assertions.assertEquals("About", article.title)
        Assertions.assertEquals(EntityStatus.ACTIVE, article.status)
        Assertions.assertTrue(article.body!!.startsWith("##  The site"))
    }

    @Test
    fun `Get a nonexistent article`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.ARTICLE, hashMapOf(Pair(PolicyAction.READ, true)))))
        val response = exchangeError(entity = HttpEntity<Unit>(testHeaders()), method = HttpMethod.GET,
            url = "$baseApiUrl${ArticleController.ARTICLE_ENDPOINT}/${EntityUtils.uuid()}")
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals(504001, response.body!!.errorCode)
        Assertions.assertEquals(404, response.body!!.status)
    }

    @Test
    fun `Create an article`() {
        val article = Article(title = "Test title", body = "A test body")
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.ARTICLE, hashMapOf(Pair(PolicyAction.CREATE, true)))))
        val response = exchange(entity = HttpEntity<Article>(article),
            responseType = object : ParameterizedTypeReference<Article>() {},
            method = HttpMethod.POST,
            url = "$baseApiUrl${ArticleController.ARTICLE_ENDPOINT}")
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertNotNull(response.body)
        Assertions.assertEquals(article.title, response.body!!.title)
        Assertions.assertEquals(article.body, response.body!!.body)
        Assertions.assertNotNull(response.body!!.id)
    }

    private fun createArticleWithError(article: Article): ErrorResponse {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.ARTICLE, hashMapOf(Pair(PolicyAction.CREATE, true)))))
        val response = exchangeError(entity = HttpEntity<Article>(article),
            method = HttpMethod.POST,
            url = "$baseApiUrl${ArticleController.ARTICLE_ENDPOINT}")
        Assertions.assertNotNull(response.body)
        return response.body!!
    }

    @Test
    fun `Create an article with a null or empty title`() {
        val article = Article(title = "", body = "A test body")
        val emptyResponse = createArticleWithError(article)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), emptyResponse.status)
        Assertions.assertEquals(507001, emptyResponse.errorCode)
        article.title = null
        val nullResponse = createArticleWithError(article)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), nullResponse.status)
        Assertions.assertEquals(507001, nullResponse.errorCode)
    }

    @Test
    fun `Create an article with a null or empty body`() {
        val article = Article(title = "Test title", body = "")
        val emptyResponse = createArticleWithError(article)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), emptyResponse.status)
        Assertions.assertEquals(507001, emptyResponse.errorCode)
        article.body = null
        val nullResponse = createArticleWithError(article)
        @Suppress("DuplicatedCode")
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), nullResponse.status)
        Assertions.assertEquals(507001, nullResponse.errorCode)
    }

    @Test
    fun `Delete an article`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.ARTICLE, hashMapOf(Pair(PolicyAction.DELETE, true)))))
        val response = exchange(entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<Article>() {},
            method = HttpMethod.DELETE,
            url = "$baseApiUrl${ArticleController.ARTICLE_ENDPOINT}/$aboutArticleId")
        Assertions.assertEquals(EntityStatus.DELETED, response.body!!.status)
        val deleted = getArticle(aboutArticleId)
        Assertions.assertEquals(EntityStatus.DELETED, deleted.status)
    }

    @Test
    fun `Delete a nonexistent article`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.ARTICLE, hashMapOf(Pair(PolicyAction.DELETE, true)))))
        val response = exchangeError(entity = HttpEntity<Unit>(testHeaders()),
            method = HttpMethod.DELETE,
            url = "$baseApiUrl${ArticleController.ARTICLE_ENDPOINT}/${EntityUtils.uuid()}")
        Assertions.assertNotNull(response.body)
        Assertions.assertEquals(504003, response.body!!.errorCode)
    }

    private fun doSearch(criteria: String): NoPageResponse<Article> {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.ARTICLE, hashMapOf(Pair(PolicyAction.READ, true)))))
        val response = exchange(entity = HttpEntity<Unit>(testHeaders()), method = HttpMethod.GET,
            responseType = object : ParameterizedTypeReference<NoPageResponse<Article>> () {},
            url = "$baseApiUrl${ArticleController.ARTICLE_ENDPOINT}?$criteria")
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertNotNull(response.body)
        return response.body!!
    }

    @Test
    fun `Search for all articles`() {
        val articlePages = doSearch("")
        Assertions.assertTrue(articlePages.size > 5)
        Assertions.assertTrue(articlePages.content.size > 5)
    }

    @Test
    fun `Search for a single article`() {
        val articlePages = doSearch("id=$aboutArticleId")
        Assertions.assertEquals(1, articlePages.size)
        Assertions.assertEquals(1, articlePages.content.size)
        val article = articlePages.content.first()
        Assertions.assertEquals(aboutArticleId, article.id)
        Assertions.assertEquals("About", article.title)
        Assertions.assertTrue(article.body!!.startsWith("##  The site"))
    }

    @Test
    fun `Search for multiple articles`() {
        val articlePages = doSearch("id=$changelogArticleId&name=roadmap&contents=technical experiment")
        Assertions.assertEquals(3, articlePages.size)
        Assertions.assertEquals(3, articlePages.content.size)
        val articleIds = articlePages.content.map { it.id }
        Assertions.assertTrue(articleIds.contains(aboutArticleId))
        Assertions.assertTrue(articleIds.contains(changelogArticleId))
        Assertions.assertTrue(articleIds.contains(roadmapArticleId))
    }

    @Test
    fun `Unmatched search criteria do nothing`() {
        val articlePages = doSearch("id=${EntityUtils.uuid()}&name=nothing,avoided&contents=this roadmap,unmatched criteria")
        Assertions.assertEquals(1, articlePages.size)
        Assertions.assertEquals(1, articlePages.content.size)
        Assertions.assertEquals(roadmapArticleId, articlePages.content.first().id)
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

    private fun editArticle(id: String, article: Article): Article {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.ARTICLE, hashMapOf(Pair(PolicyAction.EDIT, true)))))
        val response = exchange(entity = HttpEntity<Article>(article),
            responseType = object : ParameterizedTypeReference<Article>() {},
            method = HttpMethod.PUT,
            url = "$baseApiUrl${ArticleController.ARTICLE_ENDPOINT}/$id")
        Assertions.assertNotNull(response.body)
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        return response.body!!
    }

    private fun getRevisions(id: String): List<Article> {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.ARTICLE, hashMapOf(Pair(PolicyAction.READ, true)))))
        val response = exchange(entity = HttpEntity<Unit>(testHeaders()),
            responseType = object : ParameterizedTypeReference<List<Article>>() {},
            method = HttpMethod.GET,
            url = "$baseApiUrl${ArticleController.ARTICLE_ENDPOINT}/$id/revisions")
        Assertions.assertNotNull(response.body)
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        return response.body!!
    }

    @Test
    fun `Get empty article revisions`() {
        val revisions = getRevisions(roadmapArticleId)
        Assertions.assertEquals(0, revisions.size)
    }

    @Test
    fun `Create an article revision without publishing it`() {
        val articleUpdate = Article(body = "Changed body")
        val updated = editArticle(roadmapArticleId, articleUpdate)
        Assertions.assertEquals(updated.body, articleUpdate.body)
        Assertions.assertNotNull(updated.title)
        val revisions = getRevisions(roadmapArticleId)
        Assertions.assertEquals(1, revisions.size)
    }

    @Test
    fun `By default an article revision is not published`() {
        val articleUpdate = Article(body = "Different body")
        val revision = editArticle(roadmapArticleId, articleUpdate)
        Assertions.assertEquals(EntityStatus.INACTIVE, revision.revisionStatus)
        val article = getArticle(roadmapArticleId)
        Assertions.assertNotEquals(articleUpdate.body, article.body)
    }

    @Test
    fun `An article can't be deleted through revision`() {
        val articleUpdate = Article(body = "Not deleted", status = EntityStatus.DELETED)
        val revision = editArticle(roadmapArticleId, articleUpdate)
        Assertions.assertEquals(EntityStatus.INACTIVE, revision.revisionStatus)
    }

    @Test
    fun `An article can revision can be published through revision status`() {
        val articleUpdate = Article(body = "Change roadmap", revisionStatus = EntityStatus.ACTIVE)
        val revision = editArticle(roadmapArticleId, articleUpdate)
        Assertions.assertEquals(EntityStatus.ACTIVE, revision.revisionStatus)
        val article = getArticle(roadmapArticleId)
        Assertions.assertEquals(articleUpdate.body, article.body)
    }

    // set article inactive

    @Test
    fun `An article revision can be deleted`() {
        val articleUpdate = Article(body = "To be deleted")
        editArticle(changelogArticleId, articleUpdate)
        var revisions = getRevisions(changelogArticleId)
        Assertions.assertEquals(1, revisions.size)
        articleUpdate.revisionStatus = EntityStatus.DELETED
        editArticle(changelogArticleId, articleUpdate)
        revisions = getRevisions(changelogArticleId)
        Assertions.assertEquals(0, revisions.size)
    }

    @Test
    fun `An article can be deactivated through a revision`() {
        val articleUpdate = Article(body = "To be deactivated", status = EntityStatus.INACTIVE, revisionStatus = EntityStatus.ACTIVE)
        editArticle(changelogArticleId, articleUpdate)
        val article = getArticle(changelogArticleId)
        Assertions.assertEquals(EntityStatus.INACTIVE, article.status)
        Assertions.assertEquals(articleUpdate.body, article.body)
    }

    @Test
    fun `Committing a revision causes new edits to make new revisions`() {
        val articleUpdate = Article(body = "First change")
        editArticle(changelogArticleId, articleUpdate)
        val firstBody = "Same revision"
        val secondBody = "New revision"
        articleUpdate.body = firstBody
        articleUpdate.revisionStatus = EntityStatus.ACTIVE
        editArticle(changelogArticleId, articleUpdate)
        articleUpdate.body = secondBody
        editArticle(changelogArticleId, articleUpdate)
        val revisions = getRevisions(changelogArticleId)
        Assertions.assertEquals(2, revisions.size)
        Assertions.assertEquals(firstBody, revisions[0].body)
        Assertions.assertEquals(secondBody, revisions[1].body)
    }

}
