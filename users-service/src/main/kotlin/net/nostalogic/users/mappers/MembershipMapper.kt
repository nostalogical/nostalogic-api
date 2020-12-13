package net.nostalogic.users.mappers

import net.nostalogic.datamodel.NoDate
import net.nostalogic.users.datamodel.memberships.Membership
import net.nostalogic.users.persistence.entities.MembershipEntity

object MembershipMapper {

    fun entityToDto(entity: MembershipEntity): Membership {
        return Membership(
                userId = entity.userId,
                groupId = entity.groupId,
                username = entity.username,
                group = entity.groupName,
                role = entity.role,
                status = entity.status,
                groupType = entity.groupType,
                created = NoDate(entity.created)
        )
    }

    fun entityToUserMemberDto(entity: MembershipEntity): Membership {
        return Membership(
                userId = entity.userId,
                username = entity.username,
                role = entity.role,
                status = entity.status,
                created = NoDate(entity.created)
        )
    }

    fun entityToGroupMemberDto(entity: MembershipEntity): Membership {
        return Membership(
                groupId = entity.groupId,
                group = entity.groupName,
                role = entity.role,
                status = entity.status,
                groupType = entity.groupType,
                created = NoDate(entity.created)
        )
    }

}
