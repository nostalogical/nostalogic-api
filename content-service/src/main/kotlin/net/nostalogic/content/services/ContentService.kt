package net.nostalogic.content.services

import net.nostalogic.constants.NoLocale
import net.nostalogic.content.datamodel.ContainerType
import net.nostalogic.content.datamodel.Content
import net.nostalogic.content.mappers.ContentMapper
import net.nostalogic.content.persistence.repositories.ArticleRepository
import net.nostalogic.content.persistence.repositories.ContainerRepository
import net.nostalogic.content.persistence.repositories.NavRepository
import net.nostalogic.exceptions.NoRetrieveException
import org.apache.commons.lang3.RegExUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ContentService(
    @Autowired private val navRepository: NavRepository,
    @Autowired private val containerRepository: ContainerRepository,
    @Autowired private val articleRepository: ArticleRepository) {

    fun getNavContent(rawFullNav: String): Content<*> {
        val fullNav = sanitisePath(rawFullNav)
        val nav = navRepository.findByFullUrnEquals(fullNav)
            ?: throw NoRetrieveException(503001, "Content", "The path '$fullNav' does not exist")

        val childLinks = navRepository.findAllByParentIdEquals(nav.id)
        val container = containerRepository.findByNavigationIdAndLocale(nav.id, NoLocale.en_GB)
            ?: throw NoRetrieveException(503002, "Content", "The path '$fullNav' is not linked to any content")
        if (container.type != ContainerType.ARTICLE)
            throw NoRetrieveException(503003, "Content", "The path '$fullNav' is linked to an unsupported content type")
        val article = articleRepository.findByIdOrNull(container.resourceId)
            ?: throw NoRetrieveException(503004, "Content", "The path '$fullNav' is linked to an article that could not be found")

        return ContentMapper.articleContentToDto(article, nav, childLinks)
    }

    /**
     * Remove any leading or trailing slashes, and replace any multiple slashes with a single one
     */
    private fun sanitisePath(path: String): String {
        return RegExUtils.replacePattern(
            RegExUtils.replacePattern(path, "^/*|/*\$", "")
            , "/+", "/")
    }
}
