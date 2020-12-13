package net.nostalogic.users.datamodel.users

import net.nostalogic.datamodel.NoPageable
import net.nostalogic.datamodel.SearchCriteria
import net.nostalogic.entities.EntityStatus
import java.util.*

class UserSearchCriteria(userIds: Collection<String>? = null,
                         emails: Collection<String>? = null,
                         usernames: Collection<String>? = null,
                         memberGroupIds: Collection<String>? = null,
                         status: Collection<EntityStatus>? = null,
                         page: NoPageable<User>? = null): SearchCriteria<User> {

    companion object {
        val DEFAULT_SORT_FIELDS = arrayOf("created", "id")
    }

    val userIds: Collection<String> = userIds ?: Collections.emptySet()
    val emails: Collection<String> = emails ?: Collections.emptySet()
    val usernames: Collection<String> = usernames ?: Collections.emptySet()
    val memberGroupIds: Collection<String> = memberGroupIds ?: Collections.emptySet()
    val status: Collection<EntityStatus> = status ?: setOf(EntityStatus.ACTIVE)
    val page: NoPageable<User> = page ?: NoPageable(1, 20, *DEFAULT_SORT_FIELDS)
}
