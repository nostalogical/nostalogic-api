package net.nostalogic.users.mappers

import net.nostalogic.datamodel.NoDate
import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.datamodel.access.EntityRights
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.users.datamodel.memberships.Membership
import net.nostalogic.users.datamodel.users.User
import net.nostalogic.users.datamodel.users.UserRegistration
import net.nostalogic.users.persistence.entities.UserEntity
import net.nostalogic.utils.EntityUtils
import net.nostalogic.utils.Serialiser

object UserMapper {

    fun entityToDto(entity: UserEntity,
                    includeSensitive: Boolean = true,
                    memberships: NoPageResponse<Membership>? = null,
                    rights: Map<NoEntity, EntityRights>? = null): User {
        return User(
            id = entity.id,
            username = entity.username,
            displayName = entity.displayName,
            email = if (includeSensitive) entity.email else null,
            status = entity.status,
            memberships = memberships,
            details = entity.details?.let { Serialiser.toJsonObject(it) },
            rights = rights,
            created = NoDate(entity.created),
        )
    }

    fun registrationToEntity(
            userRegistration: UserRegistration,
            creatorId: String? = null,
            status: EntityStatus = EntityStatus.INACTIVE,
            tag: String?,
    ): UserEntity {
        val userId = EntityUtils.uuid()
        return UserEntity(
            id = userId,
            creatorId = creatorId ?: userId,
            username = usernameWithTag(userRegistration.username!!, tag),
            displayName = userRegistration.username,
            email = userRegistration.email!!,
            status = status,
            locale = userRegistration.locale,
            )
    }

    fun usernameWithTag(baseUsername: String, tag: String?): String {
        return tag?.let { "${baseUsername}#${it}" } ?: baseUsername
    }

}
