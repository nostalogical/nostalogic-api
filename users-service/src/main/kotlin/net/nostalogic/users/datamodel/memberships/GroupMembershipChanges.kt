package net.nostalogic.users.datamodel.memberships

import net.nostalogic.users.constants.MembershipStatus

data class GroupMembershipChanges(
        val groupId: String,
        val memberships: HashSet<MembershipChange> = HashSet()
) {

    fun addMembershipChange(userId: String, changed: Boolean = false, oldStatus: MembershipStatus? = null,
                            newStatus: MembershipStatus? = null, reasonKey: String? = null) {
        memberships.add(MembershipChange(userId, changed, oldStatus, if (changed) newStatus else oldStatus, reasonKey))
    }

}
