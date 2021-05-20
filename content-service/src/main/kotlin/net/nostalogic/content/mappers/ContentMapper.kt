package net.nostalogic.content.mappers

import net.nostalogic.content.datamodel.ContainerType
import net.nostalogic.content.datamodel.Content
import net.nostalogic.content.datamodel.articles.Article
import net.nostalogic.content.datamodel.navigations.NavDetails
import net.nostalogic.content.persistence.entities.ArticleEntity

object ContentMapper {

    fun articleContentToDto(article: ArticleEntity, navDetails: NavDetails): Content<Article> {
        return Content(
            path = navDetails.fullPath!!,
            breadcrumbs = navDetails.breadcrumbs,
            topLinks = navDetails.topLinks,
            sideLinks = navDetails.sideLinks,
            type = ContainerType.ARTICLE,
            content = ArticleMapper.articleEntityToDto(article))
    }

}
