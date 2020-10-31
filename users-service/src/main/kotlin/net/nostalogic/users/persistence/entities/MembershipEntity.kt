package net.nostalogic.users.persistence.entities

import net.nostalogic.persistence.entities.AbstractCoreEntity
import net.nostalogic.users.constants.MembershipStatus
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity(name = "membership")
class MembershipEntity(
        val userId: String,
        val groupId: String,
        @Enumerated(EnumType.STRING) val status: MembershipStatus
): AbstractCoreEntity()
