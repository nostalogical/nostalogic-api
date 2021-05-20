package net.nostalogic.content.persistence.repositories

import net.nostalogic.content.persistence.entities.ArticleRevisionEntity
import net.nostalogic.entities.EntityStatus
import org.springframework.data.jpa.repository.JpaRepository

interface ArticleRevisionRepository: JpaRepository<ArticleRevisionEntity, String> {

    fun getAllByArticleIdAndStatusInOrderByCreated(articleId: String, status: Collection<EntityStatus>): List<ArticleRevisionEntity>
    fun getFirstByArticleIdAndCreatorIdAndStatusEquals(articleId: String, creatorId: String, status: EntityStatus): ArticleRevisionEntity?

}
