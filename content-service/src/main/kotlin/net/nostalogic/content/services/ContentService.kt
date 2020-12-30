package net.nostalogic.content.services

import net.nostalogic.constants.NoLocale
import net.nostalogic.content.datamodel.ContainerType
import net.nostalogic.content.datamodel.Content
import net.nostalogic.content.mappers.ContentMapper
import net.nostalogic.content.persistence.repositories.ArticleRepository
import net.nostalogic.content.persistence.repositories.ContainerRepository
import net.nostalogic.exceptions.NoRetrieveException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ContentService(
    @Autowired private val navService: NavService,
    @Autowired private val containerRepository: ContainerRepository,
    @Autowired private val articleRepository: ArticleRepository) {

    fun getContentAtNavigation(rawPath: String): Content<*> {
        val navDetails = navService.getNavDetails(rawPath)
        if (navDetails.system)
            throw NoRetrieveException(503002, "Page", "The path '$rawPath' is a system path and cannot be linked to any content")
        val container = containerRepository.findByNavigationIdAndLocale(navDetails.navId, NoLocale.en_GB)
            ?: throw NoRetrieveException(503003, "Content", "The path '$rawPath' is not linked to any content")
        if (container.type != ContainerType.ARTICLE)
            throw NoRetrieveException(503004, "Content", "The path '$rawPath' is linked to an unsupported content type")
        val article = articleRepository.findByIdOrNull(container.resourceId)
            ?: throw NoRetrieveException(503005, "Content", "The path '$rawPath' is linked to an article that could not be found")

        return ContentMapper.articleContentToDto(article, navDetails)
    }

}
