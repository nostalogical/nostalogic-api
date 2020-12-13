package net.nostalogic.users.mappers

import net.nostalogic.datamodel.authentication.UserAuthentication
import net.nostalogic.security.contexts.SessionContext
import net.nostalogic.users.persistence.entities.AuthenticationEntity
import java.sql.Timestamp

object AuthMapper {

    fun newAuthToEntity(dto: UserAuthentication, userId: String): AuthenticationEntity {
        return AuthenticationEntity(
                userId = userId,
                hash = dto.hash,
                salt = dto.salt,
                iterations = dto.iterations,
                encoder = dto.encoder,
                creatorId = SessionContext.getUserId())
    }

    fun tempAuthToEntity(dto: UserAuthentication, userId: String, tempReason: String): AuthenticationEntity {
        return AuthenticationEntity(
                userId = userId,
                hash = dto.hash,
                salt = dto.salt,
                iterations = dto.iterations,
                encoder = dto.encoder,
                expiration = Timestamp(System.currentTimeMillis()),
                expired = true,
                expiredReason = tempReason,
                creatorId = SessionContext.getUserId())
    }

}
