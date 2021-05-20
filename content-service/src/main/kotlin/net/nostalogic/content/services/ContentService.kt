package net.nostalogic.content.services

import net.nostalogic.constants.NoLocale
import net.nostalogic.content.datamodel.ContainerType
import net.nostalogic.content.datamodel.Content
import net.nostalogic.content.datamodel.containers.Container
import net.nostalogic.content.mappers.ContentMapper
import net.nostalogic.content.persistence.repositories.ArticleRepository
import net.nostalogic.content.persistence.repositories.ContainerRepository
import net.nostalogic.content.persistence.repositories.NavRepository
import net.nostalogic.content.validators.ContainerValidator
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoRetrieveException
import net.nostalogic.utils.EntityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ContentService(
    @Autowired private val navService: NavService,
    @Autowired private val navRepository: NavRepository,
    @Autowired private val articleService: ArticleService,
    @Autowired private val containerRepository: ContainerRepository,
    @Autowired private val articleRepository: ArticleRepository) {

    fun getContentAtNavigation(rawPath: String): Content<*> {
        val navDetails = navService.getNavDetails(rawPath)
        if (navDetails.system!!)
            throw NoRetrieveException(503002, "Page", "The path '$rawPath' is a system path and cannot be linked to any content")
        val container = containerRepository.findByNavigationIdAndLocale(navDetails.navId!!, NoLocale.en_GB)
            ?: throw NoRetrieveException(503003, "Content", "The path '$rawPath' is not linked to any content")
        if (container.type != ContainerType.ARTICLE)
            throw NoRetrieveException(503004, "Content", "The path '$rawPath' is linked to an unsupported content type")
        val article = articleRepository.findByIdOrNull(container.resourceId)
            ?: throw NoRetrieveException(503005, "Content", "The path '$rawPath' is linked to an article that could not be found")

        return ContentMapper.articleContentToDto(article, navDetails)
    }

    fun setContent(container: Container): Container {
        // Pull the nav, check it exists, if it does look for existing containers
        // Check the current user can create

        ContainerValidator.validate(container)



        val contentType = EntityUtils.toEntity(container.type!!)
        val nav = navRepository.findByIdOrNull(container.navId!!)
            ?: throw NoRetrieveException(503007, "Navigation", "The navigation '${container.navId!!}' does not exist")
        val query = AccessQuery().currentSubject()
            .addQuery(EntityReference(nav.id, NoEntity.NAV), PolicyAction.EDIT, PolicyAction.EDIT_OWN)

        if (contentType == NoEntity.ARTICLE) {
            val article = articleService.getArticle(container.contentId!!)
            query.addQuery(EntityReference(article.id, NoEntity.ARTICLE), PolicyAction.READ)
        }

        val existingContainer = containerRepository.findByNavigationId(nav.id)

        // Ties existing content of some type to an existing nav
        // Need a container type, a nav ID, and the content details - article ID for now

        // Get the nav
        // Check if it has a container attached already
        // If it doesn't, check create permission, if it does, check edit permissions
        return container
    }

}
