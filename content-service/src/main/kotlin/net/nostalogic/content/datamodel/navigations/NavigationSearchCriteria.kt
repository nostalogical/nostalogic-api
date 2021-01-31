package net.nostalogic.content.datamodel.navigations

import net.nostalogic.datamodel.NoPageable
import net.nostalogic.datamodel.SearchCriteria
import net.nostalogic.entities.EntityStatus
import java.util.*

class NavigationSearchCriteria(navIds: Collection<String>? = null,
                               status: Collection<EntityStatus>? = null,
                               page: NoPageable<Nav>? = null): SearchCriteria<Nav> {

    companion object {
        val DEFAULT_SORT_FIELDS = arrayOf("created", "id")
    }

    val navIds: Collection<String> = navIds ?: Collections.emptySet()
    val status: Collection<EntityStatus> = status ?: setOf(EntityStatus.ACTIVE)
    val page: NoPageable<Nav> = page ?: NoPageable(1, 20, *DEFAULT_SORT_FIELDS)
}
