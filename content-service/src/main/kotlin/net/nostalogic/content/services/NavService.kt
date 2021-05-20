package net.nostalogic.content.services

import net.nostalogic.content.datamodel.navigations.Nav
import net.nostalogic.content.datamodel.navigations.NavDetails
import net.nostalogic.content.datamodel.navigations.NavigationSearchCriteria
import net.nostalogic.content.mappers.NavMapper
import net.nostalogic.content.persistence.entities.NavEntity
import net.nostalogic.content.persistence.repositories.ContainerRepository
import net.nostalogic.content.persistence.repositories.NavLinkRepository
import net.nostalogic.content.persistence.repositories.NavRepository
import net.nostalogic.content.utils.PathUtils
import net.nostalogic.content.validators.NavValidator
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoAccessException
import net.nostalogic.exceptions.NoDeleteException
import net.nostalogic.exceptions.NoRetrieveException
import net.nostalogic.exceptions.NoSaveException
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class NavService(
    @Autowired private val navRepository: NavRepository,
    @Autowired private val navLinkRepository: NavLinkRepository,
    @Autowired private val containerRepository: ContainerRepository) {

    private val logger = LoggerFactory.getLogger(NavService::class.java)

    fun getNavDetails(rawPath: String): NavDetails {
        val nav = getPathNavigation(rawPath)
        var childNavs = navRepository.findAllByNavLinks(nav.id)
        val childNavLinks = navLinkRepository.findAllByParentIdEqualsAndStatusIn(nav.id, setOf(EntityStatus.ACTIVE))

        val navIds = hashSetOf(nav.id)
        navIds.addAll(childNavs.map { it.id })
        val report = AccessQuery().addQuery(resourceId = navIds, entity = NoEntity.NAV, PolicyAction.READ).toReport()

        if (!report.hasPermission(EntityReference(id = nav.id, entity = NoEntity.NAV), PolicyAction.READ))
            throw NoAccessException(501010, "Missing rights to view this navigation")

        childNavs = childNavs.filter { report.hasPermission(EntityReference(id = it.id, entity = NoEntity.NAV), PolicyAction.READ) }

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
        if (StringUtils.isNotBlank(nav.parentId)) {
            val parent = navRepository.findByIdOrNull(nav.parentId)
                ?: throw NoRetrieveException(504008, "Navigation", "Parent navigation ${nav.parentId} not found in database")
            nav.path = "${parent.fullUrn}/${nav.path!!.split("/").last()}"
        }
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

        if (StringUtils.isNotBlank(nav.text))
            entity.text = nav.text!!
        if (StringUtils.isNotBlank(nav.icon))
            entity.icon = nav.icon!!
        if (nav.status != null && nav.status != EntityStatus.DELETED)
            entity.status = nav.status!!

        if (StringUtils.isNotBlank(nav.path) && !nav.path.equals(entity.fullUrn, true)) {
            val newUrn = nav.path!!.split("/").last()
            val originalPath = entity.fullUrn
            val newPath = originalPath.replace(originalPath.split("/").last(), newUrn)
            entity.urn = newUrn

            forNavAndChildren(entity, object : RecursiveNavProcess {
                override fun execute(navEntity: NavEntity) {
                    if (navEntity.fullUrn.startsWith(originalPath)) {
                        navEntity.fullUrn = navEntity.fullUrn.replaceFirst(originalPath, newPath)
                        saveNav(navEntity)
                    }
                }
            })
        }

        return NavMapper.entityToDto(navRepository.findByIdOrNull(navId)!!)
    }

    fun deleteNav(navId: String) {
        val entity = navRepository.findByIdOrNull(navId)
            ?: throw NoRetrieveException(504006, "Navigation", "Navigation $navId not found in database")
        if (!AccessQuery().simpleCheck(id = navId, entity = NoEntity.NAV, action = PolicyAction.DELETE, creatorId = entity.creatorId))
            throw NoAccessException(501008, "Missing rights to delete navigation")
        forNavAndChildren(entity, object : RecursiveNavProcess {
            override fun execute(navEntity: NavEntity) {
                try {
                    containerRepository.deleteAllByNavigationId(navEntity.id)
                    navLinkRepository.deleteAllByChildIdEqualsOrParentIdEquals(navEntity.id, navEntity.id)
                    navRepository.delete(navEntity)
                } catch (e: Exception) {
                    logger.error("Unable to delete navigation ${navEntity.id}", e)
                    throw NoDeleteException(code = 503006, objectName = "navigation", cause = e)
                }
            }
        })
    }

    interface RecursiveNavProcess {
        fun execute(navEntity: NavEntity)
    }

    private fun forNavAndChildren(navEntity: NavEntity, process: RecursiveNavProcess) {
        val childEntities = navRepository.findAllByParentIdIs(navEntity.id)
        for (child in childEntities)
            forNavAndChildren(child, process)
        process.execute(navEntity)
    }

    fun getNav(navId: String): Nav {
        if (!AccessQuery().simpleCheck(id = navId, entity = NoEntity.NAV, action = PolicyAction.READ))
            throw NoAccessException(501007, "Missing rights to view navigation")
        val entity = navRepository.findByIdOrNull(navId)
            ?: throw NoRetrieveException(504005, "Navigation", "Navigation $navId not found in database")
        return NavMapper.entityToDto(entity)
    }

    fun searchNavs(searchCriteria: NavigationSearchCriteria): List<Nav> {
        val query = AccessQuery().currentSubject()
            .addQuery(null, NoEntity.NAV, PolicyAction.READ)
            .addQuery(null, NoEntity.NAV, PolicyAction.EDIT)
            .addQuery(null, NoEntity.NAV, PolicyAction.EDIT_OWN)
        if (searchCriteria.navIds.isNotEmpty()) {
            query.addQuery(searchCriteria.navIds, NoEntity.NAV, PolicyAction.READ)
            query.addQuery(searchCriteria.navIds, NoEntity.NAV, PolicyAction.EDIT)
        }
        val report = query.toReport()

        val navIds: Set<String> = if (searchCriteria.navIds.isEmpty() && report.hasPermission(EntityReference(entity = NoEntity.NAV), PolicyAction.READ)) emptySet()
        else if (searchCriteria.navIds.isEmpty()) report.permittedForEntity(NoEntity.NAV, PolicyAction.READ)
        else report.filterByPermitted(searchCriteria.navIds, NoEntity.NAV, PolicyAction.READ)

        val status = searchCriteria.status.map { it.name }.toSet()
        val urns = if (searchCriteria.urns.isEmpty()) "" else "%${searchCriteria.urns.joinToString("%,%")}%"
        val texts = if (searchCriteria.texts.isEmpty()) "" else "%${searchCriteria.texts.joinToString("%,%")}%"
        val page = searchCriteria.page.toQuery()

        val navEntities =
            when {
                navIds.isEmpty() && urns.isBlank() && texts.isBlank() -> navRepository.searchNavs(status, page)
                else -> navRepository.searchNavsByFields(navIds, urns, texts, status, page)
            }
        searchCriteria.page.setResponseMetadata(navEntities)

        return navEntities.map { NavMapper.entityToDto(it) }.toList()
    }

    private fun saveNav(navEntity: NavEntity): NavEntity {
        return try {
            navRepository.save(navEntity)
        } catch (e: Exception) {
            logger.error("Unable to save navigation ${navEntity.id}", e)
            throw NoSaveException(505003, "navigation", e, status = HttpStatus.CONFLICT)
        }
    }

}
