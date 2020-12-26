package net.nostalogic.content.mappers

import net.nostalogic.content.datamodel.Nav
import net.nostalogic.content.datamodel.NavDetails
import net.nostalogic.content.datamodel.NavType
import net.nostalogic.content.persistence.entities.NavEntity

object NavMapper {

    fun entitiesToDetailsDto(navEntity: NavEntity, childEntities: Collection<NavEntity>): NavDetails {
        val top = ArrayList<Nav>()
        val side = ArrayList<Nav>()
        val sortedChildren = childEntities.sortedBy { it.ordinal }
        for (child in sortedChildren) {
            if (child.type == NavType.SIDE)
                side.add(entityToDto(child))
            else if (child.type == NavType.TOP)
                top.add(entityToDto(child))
        }
        return NavDetails(
            fullPath = navEntity.fullUrn,
            urn = navEntity.urn,
            breadcrumbs = navEntity.fullUrn.split("/"),
            top = top,
            side = side)
    }

    private fun entityToDto(navEntity: NavEntity): Nav {
        return Nav(
            text = navEntity.text,
            icon = navEntity.icon,
            path = navEntity.fullUrn)
    }

}
