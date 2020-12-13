package net.nostalogic.users.datamodel.memberships

import com.fasterxml.jackson.annotation.JsonInclude
import net.nostalogic.datamodel.NoDate
import net.nostalogic.users.constants.GroupType
import net.nostalogic.users.constants.MembershipRole
import net.nostalogic.users.constants.MembershipStatus

@JsonInclude(JsonInclude.Include.NON_NULL)
class Membership(
        val userId: String? = null,
        val groupId: String? = null,
        val username: String? = null,
        val group: String? = null,
        val role: MembershipRole?,
        val status: MembershipStatus?,
        val groupType: GroupType? = null,
        val created: NoDate? = null
)
