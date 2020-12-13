package net.nostalogic.users.datamodel.groups

import com.google.gson.JsonObject
import net.nostalogic.datamodel.access.EntityPermission
import net.nostalogic.entities.EntityStatus
import net.nostalogic.users.constants.GroupType
import net.nostalogic.users.datamodel.memberships.Membership

class Group(
        val id: String? = null,
        val name: String? = null,
        val description: String? = null,
        var status: EntityStatus? = EntityStatus.ACTIVE,
        var type: GroupType? = GroupType.USER,
        var details: JsonObject? = null,
        var memberships: List<Membership>? = null,
        var permissions: Collection<EntityPermission>? = null
)
