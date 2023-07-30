package net.nostalogic.users.datamodel.memberships

import net.nostalogic.datamodel.NoPageable
import net.nostalogic.datamodel.SearchCriteria
import net.nostalogic.users.constants.GroupType
import net.nostalogic.users.constants.MembershipStatus
import java.util.*

class MembershipSearchCriteria(
    val rights: Boolean? = null,
    userIds: Collection<String>? = null,
    groupIds: Collection<String>? = null,
    type: Collection<GroupType>? = null,
    status: Collection<MembershipStatus>? = null,
    page: NoPageable<Membership>? = null
): SearchCriteria<Membership> {

    companion object {
        val BULK_SORT_FIELDS = arrayOf("created", "role", "id")
        val SINGLE_SORT_FIELDS = arrayOf("role", "created", "id")
    }

    val userIds: Collection<String> = userIds ?: Collections.emptySet()
    val groupIds: Collection<String> = groupIds ?: Collections.emptySet()
    val type: Collection<GroupType> = type ?: GroupType.values().toSet()
    val status: Collection<MembershipStatus> = status ?: MembershipStatus.values().toSet()
    val page: NoPageable<Membership> = page ?: NoPageable(1, 20, *BULK_SORT_FIELDS)
}
