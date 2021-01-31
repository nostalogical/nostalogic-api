package net.nostalogic.content.mappers

import net.nostalogic.content.datamodel.articles.Article
import net.nostalogic.content.persistence.entities.ArticleEntity
import net.nostalogic.content.persistence.entities.ArticleRevisionEntity
import net.nostalogic.datamodel.NoDate
import net.nostalogic.entities.EntityStatus
import net.nostalogic.security.contexts.SessionContext
import java.sql.Timestamp

object ArticleMapper {

    fun articleEntityToDto(articleEntity: ArticleEntity): Article {
        return Article(
            id = articleEntity.id,
            title = articleEntity.name,
            body = articleEntity.contents,
            status = articleEntity.status,
            created = NoDate(articleEntity.created),
            creatorId = articleEntity.creatorId,
            lastUpdated = if (articleEntity.lastUpdated == articleEntity.created) NoDate(articleEntity.lastUpdated) else null,
            lastUpdaterId = if (articleEntity.lastUpdated == articleEntity.created) articleEntity.lastUpdaterId else null)
    }

    fun articleDtoToEntity(article: Article): ArticleEntity {
        val timestamp = Timestamp(System.currentTimeMillis())
        val creator = SessionContext.getUserId()
        return ArticleEntity(
            name = article.title!!,
            contents = article.body!!,
            status = article.status ?: EntityStatus.INACTIVE,
            lastUpdated = timestamp,
            lastUpdaterId = creator,
            creatorId =  creator,
        )
    }

    fun revisionEntityToDto(revisionEntity: ArticleRevisionEntity): Article {
        return Article(
            id = revisionEntity.articleId,
            revision = revisionEntity.id,
            title = revisionEntity.name,
            body = revisionEntity.contents,
            revisionStatus = revisionEntity.status,
            created = NoDate(revisionEntity.created),
            creatorId = revisionEntity.creatorId,
            lastUpdated = NoDate(revisionEntity.lastUpdated),
            lastUpdaterId = revisionEntity.creatorId
        )
    }

    fun revisionFromArticleAndUpdate(articleEntity: ArticleEntity, revision: Article, editorId: String): ArticleRevisionEntity {
        val entity = ArticleRevisionEntity(
            name = articleEntity.name,
            contents = articleEntity.contents,
            status = EntityStatus.INACTIVE,
            articleId = articleEntity.id,
            creatorId = editorId
        )
        updateRevision(revision, entity)
        return entity
    }

    fun updateRevision(revision: Article, entity: ArticleRevisionEntity) {
        if (!revision.title.isNullOrBlank())
            entity.name = revision.title!!
        if (!revision.body.isNullOrBlank())
            entity.contents = revision.body!!
        if (revision.revisionStatus != null)
            entity.status = revision.revisionStatus!!
        entity.lastUpdated = Timestamp(System.currentTimeMillis())
    }

}
