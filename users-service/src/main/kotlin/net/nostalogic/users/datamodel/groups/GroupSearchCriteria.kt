package net.nostalogic.users.datamodel.groups

import net.nostalogic.datamodel.NoPageable
import net.nostalogic.datamodel.SearchCriteria
import net.nostalogic.entities.EntityStatus
import net.nostalogic.users.constants.GroupType
import java.util.*

class GroupSearchCriteria(groupIds: Collection<String>? = null,
                          memberUserIds: Collection<String>? = null,
                          type: Collection<GroupType>? = null,
                          status: Collection<EntityStatus>? = null,
                          page: NoPageable<Group>? = null): SearchCriteria<Group> {

    companion object {
        val DEFAULT_SORT_FIELDS = arrayOf("created", "id")
    }

    val groupIds: Collection<String> = groupIds ?: Collections.emptySet()
    val memberUserIds: Collection<String> = memberUserIds ?: Collections.emptySet()
    val type: Collection<GroupType> = type ?: GroupType.values().toSet()
    val status: Collection<EntityStatus> = status ?: setOf(EntityStatus.ACTIVE)
    val page: NoPageable<Group> = page ?: NoPageable(1, 20, *DEFAULT_SORT_FIELDS)

}
