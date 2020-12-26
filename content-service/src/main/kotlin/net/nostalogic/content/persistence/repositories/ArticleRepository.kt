package net.nostalogic.content.persistence.repositories

import net.nostalogic.content.persistence.entities.ArticleEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ArticleRepository: JpaRepository<ArticleEntity, String> {

}
