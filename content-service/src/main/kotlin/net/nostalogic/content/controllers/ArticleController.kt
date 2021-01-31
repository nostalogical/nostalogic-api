package net.nostalogic.content.controllers

import net.nostalogic.content.ContentApplication
import net.nostalogic.content.controllers.ArticleController.Companion.ARTICLE_ENDPOINT
import net.nostalogic.content.datamodel.articles.Article
import net.nostalogic.content.datamodel.articles.ArticleSearchCriteria
import net.nostalogic.content.services.ArticleService
import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.datamodel.NoPageable
import net.nostalogic.entities.EntityStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping(ARTICLE_ENDPOINT, produces = ["application/json"])
class ArticleController(@Autowired private val articleService: ArticleService) {

    companion object {
        const val ARTICLE_ENDPOINT = "/api/v${ContentApplication.MAJOR}/cms/articles"
    }

    @RequestMapping(method = [RequestMethod.POST])
    fun createArticle(@RequestBody article: Article): Article {
        return articleService.createArticle(article)
    }

    @RequestMapping(method = [RequestMethod.PUT], path = ["/{articleId}"])
    fun editArticle(@PathVariable articleId: String, @RequestBody article: Article): Article {
        article.id = articleId
        return articleService.editArticle(article)
    }

    @RequestMapping(method = [RequestMethod.DELETE], path = ["/{articleId}"])
    fun deleteArticle(@PathVariable articleId: String): Article {
        return articleService.deleteArticle(articleId)
    }

    @RequestMapping(method = [RequestMethod.GET], path = ["/{articleId}"])
    fun getArticle(@PathVariable articleId: String): Article {
        return articleService.getArticle(articleId)
    }

    @RequestMapping(method = [RequestMethod.GET])
    fun searchArticles(@RequestParam(defaultValue = "1") page: Int, @RequestParam(defaultValue = "20") size: Int,
                       @RequestParam id: Set<String>?,
                       @RequestParam name: Set<String>?,
                       @RequestParam contents: Set<String>?,
                       @RequestParam status: Set<EntityStatus>?): NoPageResponse<Article> {
        val pageable = NoPageable<Article>(page, size, *ArticleSearchCriteria.DEFAULT_SORT_FIELDS)
        val result = articleService.searchArticles(ArticleSearchCriteria(articleIds = id, name = name, contents = contents, status = status, page = pageable))
        return pageable.toResponse(result)
    }

}
