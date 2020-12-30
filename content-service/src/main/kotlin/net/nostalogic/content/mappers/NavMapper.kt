package net.nostalogic.content.mappers

import net.nostalogic.content.datamodel.Nav
import net.nostalogic.content.datamodel.NavDetails
import net.nostalogic.content.datamodel.NavType
import net.nostalogic.content.persistence.entities.NavEntity
import net.nostalogic.content.persistence.entities.NavLinkEntity

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
                topLinks.add(entityToDto(mappedEntities[navLink.childId]!!))
            else
                sideLinks.add(entityToDto(mappedEntities[navLink.childId]!!))
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

    private fun entityToDto(navEntity: NavEntity): Nav {
        return Nav(
            text = navEntity.text,
            icon = navEntity.icon,
            path = navEntity.fullUrn)
    }

}
