package net.nostalogic.content.persistence.repositories

import net.nostalogic.content.persistence.entities.ArticleRevisionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ArticleRevisionRepository: JpaRepository<ArticleRevisionEntity, String> {
}
