package net.nostalogic.content.services

import net.nostalogic.content.datamodel.NavDetails
import net.nostalogic.content.mappers.NavMapper
import net.nostalogic.content.persistence.entities.NavEntity
import net.nostalogic.content.persistence.repositories.NavLinkRepository
import net.nostalogic.content.persistence.repositories.NavRepository
import net.nostalogic.entities.EntityStatus
import net.nostalogic.exceptions.NoRetrieveException
import org.apache.commons.lang3.RegExUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class NavService(
    @Autowired private val navRepository: NavRepository,
    @Autowired private val navLinkRepository: NavLinkRepository) {

    fun getNavDetails(rawPath: String): NavDetails {
        val nav = getPathNavigation(rawPath)
        val childNavs = navRepository.findAllByNavLinks(nav.id)
        val childNavLinks = navLinkRepository.findAllByParentIdEqualsAndStatusIn(nav.id, setOf(EntityStatus.ACTIVE))
        return NavMapper.entitiesToDto(nav, childNavLinks, childNavs)
    }

    private fun getPathNavigation(rawFullPath: String): NavEntity {
        val fullPath = sanitisePath(rawFullPath)
        return navRepository.findByFullUrnEquals(fullPath)
            ?: throw NoRetrieveException(503001, "Page", "The path '$fullPath' does not exist")
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
