package net.nostalogic.users.datamodel.memberships

import net.nostalogic.users.constants.MembershipStatus

data class MembershipChange(
    val userId: String? = null,
    val changed: Boolean = false,
    val oldStatus: MembershipStatus? = null,
    val newStatus: MembershipStatus? = null,
    val failReason: String? = null)
