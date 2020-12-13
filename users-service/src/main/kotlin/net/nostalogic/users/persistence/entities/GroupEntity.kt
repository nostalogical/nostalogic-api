package net.nostalogic.users.persistence.entities

import net.nostalogic.entities.EntityStatus
import net.nostalogic.persistence.entities.AbstractCoreEntity
import net.nostalogic.users.constants.GroupType
import net.nostalogic.utils.EntityUtils
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Table

@Entity
@Table(name = "\"group\"")
class GroupEntity(
        var name: String,
        var description: String?,
        @Enumerated(EnumType.STRING) var type: GroupType,
        @Enumerated(EnumType.STRING) var status: EntityStatus,
        creatorId: String = EntityUtils.SYSTEM_ID
): AbstractCoreEntity(creatorId = creatorId)
