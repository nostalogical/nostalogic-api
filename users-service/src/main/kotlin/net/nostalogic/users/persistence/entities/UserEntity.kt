package net.nostalogic.users.persistence.entities

import net.nostalogic.constants.NoLocale
import net.nostalogic.entities.EntityStatus
import net.nostalogic.persistence.entities.AbstractCoreEntity
import net.nostalogic.utils.EntityUtils
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Table

@Entity
@Table(name = "\"user\"")
class UserEntity(
        id: String = EntityUtils.uuid(),
        creatorId: String = EntityUtils.SYSTEM_ID,
        var username: String,
        var email: String,
        @Enumerated(EnumType.STRING) val locale: NoLocale,
        @Enumerated(EnumType.STRING) var status: EntityStatus
): AbstractCoreEntity(id = id, creatorId = creatorId)
