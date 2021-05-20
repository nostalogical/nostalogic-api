package net.nostalogic.content.services

import net.nostalogic.content.datamodel.articles.Article
import net.nostalogic.content.datamodel.articles.ArticleSearchCriteria
import net.nostalogic.content.mappers.ArticleMapper
import net.nostalogic.content.persistence.entities.ArticleEntity
import net.nostalogic.content.persistence.entities.ArticleRevisionEntity
import net.nostalogic.content.persistence.repositories.ArticleRepository
import net.nostalogic.content.persistence.repositories.ArticleRevisionRepository
import net.nostalogic.content.validators.ArticleValidator
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoAccessException
import net.nostalogic.exceptions.NoRetrieveException
import net.nostalogic.exceptions.NoSaveException
import net.nostalogic.security.contexts.SessionContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ArticleService(
    @Autowired private val articleRepository: ArticleRepository,
    @Autowired private val revisionRepository: ArticleRevisionRepository
    ) {

    private val logger = LoggerFactory.getLogger(ArticleService::class.java)

    fun createArticle(article: Article): Article {
        if (!AccessQuery().simpleCheck(entity = NoEntity.ARTICLE, action = PolicyAction.CREATE))
            throw NoAccessException(501004, "Missing rights to create articles")

        ArticleValidator.validateArticle(article)
        var entity = ArticleMapper.articleDtoToEntity(article)
        entity = saveArticle(entity)
        return ArticleMapper.articleEntityToDto(entity)
    }

    fun editArticle(revision: Article): Article {
        SessionContext.requireLogin()
        val articleId = revision.id!!
        val editorId = SessionContext.getUserId()
        val articleEntity = articleRepository.findByIdOrNull(articleId)
            ?: throw NoRetrieveException(504004, "Article", "Article ${articleId} not found in database")
        if (!AccessQuery().simpleCheck(id = articleId, entity = NoEntity.ARTICLE, action = PolicyAction.EDIT, creatorId = revision.creatorId))
            throw NoAccessException(501005, "Missing rights to edit article")

        ArticleValidator.validateRevision(revision)
        var revisionEntity = revisionRepository.getFirstByArticleIdAndCreatorIdAndStatusEquals(articleId, editorId, EntityStatus.INACTIVE)
        if (revisionEntity == null)
            revisionEntity = ArticleMapper.revisionFromArticleAndUpdate(articleEntity, revision, editorId)
        else
            ArticleMapper.updateRevision(revision, revisionEntity)
        revisionEntity = saveRevision(revisionEntity)

        val statusChanged = revision.status != null && revision.status != EntityStatus.DELETED
        if (statusChanged)
            articleEntity.status = revision.status!!

        if (revisionEntity.status == EntityStatus.ACTIVE) {
            articleEntity.name = revisionEntity.name
            articleEntity.contents = revisionEntity.contents
            articleEntity.lastUpdated = revisionEntity.lastUpdated
            articleEntity.lastUpdaterId = revisionEntity.creatorId
        }
        if (revisionEntity.status == EntityStatus.ACTIVE || statusChanged)
            saveArticle(articleEntity)

        return ArticleMapper.revisionEntityToDto(revisionEntity)
    }

    fun deleteArticle(articleId: String): Article {
        val entity = articleRepository.findByIdOrNull(articleId)
            ?: throw NoRetrieveException(504003, "Article", "Article ${articleId} not found in database")
        if (!AccessQuery().simpleCheck(id = articleId, entity = NoEntity.ARTICLE, action = PolicyAction.DELETE, creatorId = entity.creatorId))
            throw NoAccessException(501003, "Missing rights to delete article")
        entity.status = EntityStatus.DELETED
        return ArticleMapper.articleEntityToDto(saveArticle(entity))
    }

    private fun saveArticle(articleEntity: ArticleEntity): ArticleEntity {
        return try {
            articleRepository.save(articleEntity)
        } catch (e: Exception) {
            logger.error("Unable to save article ${articleEntity.id}", e)
            throw NoSaveException(505001, "article", e)
        }
    }

    private fun saveRevision(revisionEntity: ArticleRevisionEntity): ArticleRevisionEntity {
        return try {
            revisionRepository.save(revisionEntity)
        } catch (e: Exception) {
            logger.error("Unable to save article revision ${revisionEntity.id}", e)
            throw NoSaveException(505002, "article", e)
        }
    }

    fun getArticle(articleId: String): Article {
        if (!AccessQuery().simpleCheck(id = articleId, entity = NoEntity.ARTICLE, action = PolicyAction.READ))
            throw NoAccessException(501001, "Missing rights to view article")
        val entity = articleRepository.findByIdOrNull(articleId)
            ?: throw NoRetrieveException(504001, "Article", "Article ${articleId} not found in database")
        return ArticleMapper.articleEntityToDto(entity)
    }

    fun getArticleRevisions(articleId: String): List<Article> {
        if (!AccessQuery().simpleCheck(id = articleId, entity = NoEntity.ARTICLE, action = PolicyAction.READ))
            throw NoAccessException(501002, "Missing rights to view article revisions")
        articleRepository.findByIdOrNull(articleId)
            ?: throw NoRetrieveException(504002, "Article", "Article ${articleId} not found in database")
        val revisions = revisionRepository.getAllByArticleIdAndStatusInOrderByCreated(articleId, hashSetOf(EntityStatus.ACTIVE, EntityStatus.INACTIVE))
        return revisions.map { ArticleMapper.revisionEntityToDto(it) }.toList()
    }

    fun searchArticles(searchCriteria: ArticleSearchCriteria): List<Article> {
        val query = AccessQuery().currentSubject()
            .addQuery(null, NoEntity.ARTICLE, PolicyAction.READ)
            .addQuery(null, NoEntity.ARTICLE, PolicyAction.EDIT)
            .addQuery(null, NoEntity.ARTICLE, PolicyAction.EDIT_OWN)
        if (searchCriteria.articleIds.isNotEmpty()) {
            query.addQuery(searchCriteria.articleIds, NoEntity.ARTICLE, PolicyAction.READ)
            query.addQuery(searchCriteria.articleIds, NoEntity.ARTICLE, PolicyAction.EDIT)
        }
        val report = query.toReport()

        val articleIds: Set<String> = if (searchCriteria.articleIds.isEmpty() && report.hasPermission(EntityReference(entity = NoEntity.ARTICLE), PolicyAction.READ)) emptySet()
        else if (searchCriteria.articleIds.isEmpty()) report.permittedForEntity(NoEntity.ARTICLE, PolicyAction.READ)
        else report.filterByPermitted(searchCriteria.articleIds, NoEntity.ARTICLE, PolicyAction.READ)

        val status = searchCriteria.status.map { it.name }.toSet()
        val names = if (searchCriteria.name.isEmpty()) "" else "%${searchCriteria.name.joinToString("%,%")}%"
        val contents = if (searchCriteria.contents.isEmpty()) "" else "%${searchCriteria.contents.joinToString("%,%")}%"
        val page = searchCriteria.page.toQuery()

        val articleEntity =
            when {
                articleIds.isEmpty() && names.isBlank() && contents.isBlank() -> articleRepository.searchArticles(status, page)
                else -> articleRepository.searchArticlesByFields(articleIds, names, contents, status, page)
            }
        searchCriteria.page.setResponseMetadata(articleEntity)

        return articleEntity.map { ArticleMapper.articleEntityToDto(it) }.toList()
    }

}
