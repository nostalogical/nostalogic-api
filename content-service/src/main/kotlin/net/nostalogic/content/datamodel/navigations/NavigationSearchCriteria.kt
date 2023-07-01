package net.nostalogic.content.datamodel.navigations

import net.nostalogic.datamodel.NoPageable
import net.nostalogic.datamodel.SearchCriteria
import net.nostalogic.entities.EntityStatus
import java.util.*

class NavigationSearchCriteria(navIds: Collection<String>? = null,
                               urns: Collection<String>? = null,
                               texts: Collection<String>? = null,
                               status: Collection<EntityStatus>? = null,
                               page: NoPageable<NavLink>? = null): SearchCriteria<NavLink> {

    companion object {
        val DEFAULT_SORT_FIELDS = arrayOf("created", "id")
    }

    val navIds: Collection<String> = navIds ?: Collections.emptySet()
    val urns: Collection<String> = urns ?: Collections.emptySet()
    val texts: Collection<String> = texts ?: Collections.emptySet()
    val status: Collection<EntityStatus> = status ?: setOf(EntityStatus.ACTIVE)
    val page: NoPageable<NavLink> = page ?: NoPageable(1, 20, *DEFAULT_SORT_FIELDS)
}
