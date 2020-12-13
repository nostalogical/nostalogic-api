package net.nostalogic.users.mappers

import net.nostalogic.users.datamodel.groups.Group
import net.nostalogic.users.persistence.entities.GroupEntity

object GroupMapper {

    fun entityToDto(entity: GroupEntity): Group {
        return Group(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                status = entity.status,
                type = entity.type)
    }

}
