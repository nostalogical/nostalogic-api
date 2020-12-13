package net.nostalogic.users.persistence.entities

import net.nostalogic.crypto.encoders.EncoderType
import net.nostalogic.persistence.entities.AbstractCoreEntity
import net.nostalogic.utils.EntityUtils
import java.sql.Timestamp
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity(name = "authentication")
class AuthenticationEntity(
        val userId: String,
        val hash: String,
        val salt: String,
        val iterations: Int,
        @Enumerated(EnumType.STRING) val encoder: EncoderType,
        var expiration: Timestamp? = null,
        var expired: Boolean = false,
        var invalidation: Timestamp? = null,
        var invalid: Boolean = false,
        var expiredReason: String? = null,
        creatorId: String = EntityUtils.SYSTEM_ID
): AbstractCoreEntity(creatorId = creatorId)
