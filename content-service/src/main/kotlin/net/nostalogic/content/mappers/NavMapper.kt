package net.nostalogic.content.mappers

import net.nostalogic.content.datamodel.navigations.Nav
import net.nostalogic.content.datamodel.navigations.NavDetails
import net.nostalogic.content.datamodel.navigations.NavType
import net.nostalogic.content.persistence.entities.NavEntity
import net.nostalogic.content.persistence.entities.NavLinkEntity
import net.nostalogic.entities.EntityStatus
import net.nostalogic.security.contexts.SessionContext

object NavMapper {

    fun entitiesToDto(navEntity: NavEntity, linkEntities: Collection<NavLinkEntity>, navEntities: Collection<NavEntity>): NavDetails {
        val sortedLinks = linkEntities.sortedWith(compareBy({ it.ordinal }, {it.created}, {it.parentId == navEntity.id})).toList()
        val mappedEntities = navEntities.map { it.id to it }.toMap()
        val sideLinks = ArrayList<Nav>()
        val topLinks = ArrayList<Nav>()
        for (navLink in sortedLinks) {
            if (!mappedEntities.containsKey(navLink.childId))
                continue
            if (navLink.type == NavType.TOP)
                topLinks.add(entityToDto(mappedEntities[navLink.childId]!!, includeDetails = false))
            else
                sideLinks.add(entityToDto(mappedEntities[navLink.childId]!!, includeDetails = false))
        }
        return NavDetails(
            navId = navEntity.id,
            fullPath = navEntity.fullUrn,
            urn = navEntity.urn,
            breadcrumbs = navEntity.fullUrn.split("/"),
            topLinks = topLinks,
            sideLinks = sideLinks,
            system = navEntity.system)
    }

    fun dtoToEntity(nav: Nav): NavEntity {
        return NavEntity(
            urn = nav.path!!.split("/").last(),
            fullUrn = nav.path!!,
            icon = nav.icon!!,
            text = nav.text!!,
            status = nav.status ?: EntityStatus.INACTIVE,
            creatorId = SessionContext.getUserId()
        )
    }

    fun updateEntity(nav: Nav, navEntity: NavEntity) {
        if (nav.path != null) {
            navEntity.fullUrn = nav.path!!
            navEntity.urn = nav.path!!.split("/").last()
        }
        if (nav.icon != null)
            navEntity.icon = nav.icon!!
        if (nav.text != null)
            navEntity.text = nav.text!!
    }

    fun entityToDto(navEntity: NavEntity, includeDetails: Boolean = true): Nav {
        val nav = Nav(
            text = navEntity.text,
            icon = navEntity.icon,
            path = navEntity.fullUrn)
        if (includeDetails) {
            nav.id = navEntity.id
            nav.status = navEntity.status
            nav.parentId = navEntity.parentId
        }
        return nav
    }

}
