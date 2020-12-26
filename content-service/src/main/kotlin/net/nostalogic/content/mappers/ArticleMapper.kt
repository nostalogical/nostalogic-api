package net.nostalogic.content.mappers

import net.nostalogic.content.datamodel.Article
import net.nostalogic.content.persistence.entities.ArticleEntity

object ArticleMapper {

    fun entityToDto(articleEntity: ArticleEntity): Article {
        return Article(
            title = articleEntity.name,
            body = articleEntity.contents)
    }

}
