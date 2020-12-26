package net.nostalogic.content.mappers

import net.nostalogic.content.datamodel.Article
import net.nostalogic.content.datamodel.ContainerType
import net.nostalogic.content.datamodel.Content
import net.nostalogic.content.persistence.entities.ArticleEntity
import net.nostalogic.content.persistence.entities.NavEntity

object ContentMapper {

    fun articleContentToDto(article: ArticleEntity, nav: NavEntity, links: Collection<NavEntity>): Content<Article> {
        val navDetails = NavMapper.entitiesToDetailsDto(navEntity = nav, childEntities = links)
        return Content(
            path = navDetails.fullPath,
            breadcrumbs = navDetails.breadcrumbs,
            topLinks = navDetails.top,
            sideLinks = navDetails.side,
            type = ContainerType.ARTICLE,
            content = ArticleMapper.entityToDto(article))
    }

}
