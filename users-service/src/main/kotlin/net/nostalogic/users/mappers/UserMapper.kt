package net.nostalogic.users.mappers

import net.nostalogic.datamodel.NoDate
import net.nostalogic.entities.EntityStatus
import net.nostalogic.users.datamodel.users.User
import net.nostalogic.users.datamodel.users.UserRegistration
import net.nostalogic.users.persistence.entities.UserEntity
import net.nostalogic.utils.EntityUtils

object UserMapper {

    fun entityToDto(entity: UserEntity): User {
        return User(
                id = entity.id,
                username = entity.username,
                email = entity.email,
                status = entity.status,
                created = NoDate(entity.created))
    }

    fun registrationToEntity(
            userRegistration: UserRegistration,
            creatorId: String? = null,
            status: EntityStatus = EntityStatus.INACTIVE
    ): UserEntity {
        val userId = EntityUtils.uuid()
        return UserEntity(id = userId, creatorId = creatorId ?: userId,
                username = userRegistration.username!!,
                email = userRegistration.email!!,
                status = status,
                locale = userRegistration.locale)
    }

}
