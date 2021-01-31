package net.nostalogic.content.services

import net.nostalogic.content.datamodel.navigations.Nav
import net.nostalogic.content.datamodel.navigations.NavDetails
import net.nostalogic.content.datamodel.navigations.NavigationSearchCriteria
import net.nostalogic.content.mappers.NavMapper
import net.nostalogic.content.persistence.entities.NavEntity
import net.nostalogic.content.persistence.repositories.NavLinkRepository
import net.nostalogic.content.persistence.repositories.NavRepository
import net.nostalogic.content.utils.PathUtils
import net.nostalogic.content.validators.NavValidator
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoAccessException
import net.nostalogic.exceptions.NoRetrieveException
import net.nostalogic.exceptions.NoSaveException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class NavService(
    @Autowired private val navRepository: NavRepository,
    @Autowired private val navLinkRepository: NavLinkRepository) {

    private val logger = LoggerFactory.getLogger(NavService::class.java)

    fun getNavDetails(rawPath: String): NavDetails {
        val nav = getPathNavigation(rawPath)
        val childNavs = navRepository.findAllByNavLinks(nav.id)
        val childNavLinks = navLinkRepository.findAllByParentIdEqualsAndStatusIn(nav.id, setOf(EntityStatus.ACTIVE))
        return NavMapper.entitiesToDto(nav, childNavLinks, childNavs)
    }

    private fun getPathNavigation(rawFullPath: String): NavEntity {
        val fullPath = PathUtils.sanitisePath(rawFullPath)
        return navRepository.findByFullUrnEquals(fullPath)
            ?: throw NoRetrieveException(503001, "Page", "The path '$fullPath' does not exist")
    }

    fun createNav(nav: Nav): Nav {
        if (!AccessQuery().simpleCheck(entity = NoEntity.NAV, action = PolicyAction.CREATE))
            throw NoAccessException(501006, "Missing rights to create navigations")

        NavValidator.validateNavigation(nav, create = true)
        nav.path?.let { nav.path = PathUtils.sanitisePath(it) }
        val entity = NavMapper.dtoToEntity(nav)
        return NavMapper.entityToDto(saveNav(entity))
    }

    fun editNav(nav: Nav): Nav {
        val navId = nav.id
        val entity = navRepository.findByIdOrNull(navId)
            ?: throw NoRetrieveException(504007, "Navigation", "Navigation ${navId} not found in database")
        if (!AccessQuery().simpleCheck(id = navId, entity = NoEntity.NAV, action = PolicyAction.EDIT, creatorId = entity.creatorId))
            throw NoAccessException(501009, "Missing rights to edit navigation")

        NavValidator.validateNavigation(nav)
        // Edits here have some inherent problems. If the full path can be edited then every parent path would be editable, which is bad.
        // Only the final URN should be editable. If it's edited then every nav with this one as its parent, and all their children, etc, must be updated.


        return Nav(null, null, null, null, null)
    }

    fun deleteNav(navId: String): Nav {
        val entity = navRepository.findByIdOrNull(navId)
            ?: throw NoRetrieveException(504006, "Navigation", "Navigation ${navId} not found in database")
        if (!AccessQuery().simpleCheck(id = navId, entity = NoEntity.NAV, action = PolicyAction.DELETE, creatorId = entity.creatorId))
            throw NoAccessException(501008, "Missing rights to delete navigation")
        entity.status = EntityStatus.DELETED
        return NavMapper.entityToDto(saveNav(entity))
    }

    fun getNav(navId: String): Nav {
        if (!AccessQuery().simpleCheck(id = navId, entity = NoEntity.NAV, action = PolicyAction.READ))
            throw NoAccessException(501007, "Missing rights to view navigation")
        val entity = navRepository.findByIdOrNull(navId)
            ?: throw NoRetrieveException(504005, "Navigation", "Navigation ${navId} not found in database")
        return NavMapper.entityToDto(entity)
    }

    fun searchNavs(searchCriteria: NavigationSearchCriteria): List<Nav> {
        return emptyList()
    }

    private fun saveNav(navEntity: NavEntity): NavEntity {
        return try {
            navRepository.save(navEntity)
        } catch (e: Exception) {
            logger.error("Unable to save navigation ${navEntity.id}", e)
            throw NoSaveException(505003, "navigation", e)
        }
    }

}
