package net.nostalogic.content.persistence.repositories

import net.nostalogic.content.persistence.entities.ArticleEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ArticleRepository: JpaRepository<ArticleEntity, String> {

    @Query(value = "SELECT a.* FROM article a WHERE (a.id IN (:articleIds) OR a.name ILIKE ANY (string_to_array(:names, ',')) OR a.contents ILIKE ANY (string_to_array(:contents, ','))) AND a.status IN (:status)", nativeQuery = true)
    fun searchArticlesByFields(articleIds: Collection<String>, names: String, contents: String, status: Collection<String>, page: Pageable): Page<ArticleEntity>

    @Query(value = "SELECT a.* FROM article a WHERE a.status IN (:status)", nativeQuery = true)
    fun searchArticles(status: Collection<String>, page: Pageable): Page<ArticleEntity>

}
