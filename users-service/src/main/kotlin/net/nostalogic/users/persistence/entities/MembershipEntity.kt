package net.nostalogic.users.persistence.entities

import net.nostalogic.persistence.entities.AbstractCoreEntity
import net.nostalogic.users.constants.GroupType
import net.nostalogic.users.constants.MembershipRole
import net.nostalogic.users.constants.MembershipStatus
import javax.persistence.*

@Entity(name = "membership")
@SecondaryTables(value = [SecondaryTable(name = "\"group\""), SecondaryTable(name = "\"user\"")])
class MembershipEntity(
        val userId: String,
        val groupId: String,

        @Column(name = "name", table = "\"group\"", insertable = false, updatable = false)
        val groupName: String? = null,

        @Column(name = "username", table = "\"user\"", insertable = false, updatable = false)
        val username: String? = null,

        @Enumerated(EnumType.STRING)
        @Column(name = "type", table = "\"group\"", insertable = false, updatable = false)
        val groupType: GroupType? = null,

        @Enumerated(EnumType.STRING) var status: MembershipStatus,

        @Enumerated(EnumType.STRING) var role: MembershipRole,
        creatorId: String
): AbstractCoreEntity(creatorId = creatorId)
