package net.nostalogic.content.mappers

import net.nostalogic.content.datamodel.navigations.NavLink
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
        val sideLinks = ArrayList<NavLink>()
        val topLinks = ArrayList<NavLink>()
        for (navLink in sortedLinks) {
            if (!mappedEntities.containsKey(navLink.childId))
                continue
            if (navLink.type == NavType.TOP)
                topLinks.add(entityToDto(mappedEntities[navLink.childId]!!, includeDetails = false))
            else
                sideLinks.add(entityToDto(mappedEntities[navLink.childId]!!, includeDetails = false))
        }
        val urnSplit = navEntity.fullUrn.split("/")
        val breadcrumbs = if (urnSplit.size == 1 && urnSplit[0] == "") emptyList() else urnSplit
        return NavDetails(
            navId = navEntity.id,
            fullPath = navEntity.fullUrn,
            urn = navEntity.urn,
            breadcrumbs = breadcrumbs,
            topLinks = topLinks,
            sideLinks = sideLinks,
            system = navEntity.system)
    }

    fun dtoToEntity(nav: NavLink): NavEntity {
        return NavEntity(
            urn = nav.path!!.split("/").last(),
            fullUrn = nav.path!!,
            parentId = nav.parentId,
            icon = nav.icon!!,
            text = nav.text!!,
            status = nav.status ?: EntityStatus.INACTIVE,
            creatorId = SessionContext.getUserId()
        )
    }

    fun updateEntity(nav: NavLink, navEntity: NavEntity) {
        if (nav.path != null) {
            navEntity.fullUrn = nav.path!!
            navEntity.urn = nav.path!!.split("/").last()
        }
        if (nav.icon != null)
            navEntity.icon = nav.icon!!
        if (nav.text != null)
            navEntity.text = nav.text!!
    }

    fun entityToDto(navEntity: NavEntity, includeDetails: Boolean = true): NavLink {
        val nav = NavLink(
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
