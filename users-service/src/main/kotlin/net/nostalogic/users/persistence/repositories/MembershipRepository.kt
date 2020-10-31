package net.nostalogic.users.persistence.repositories

import net.nostalogic.users.constants.MembershipStatus
import net.nostalogic.users.persistence.entities.MembershipEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MembershipRepository: JpaRepository<MembershipEntity, String> {

    fun findAllByUserIdEqualsAndStatusIn(userId: String, status: Collection<MembershipStatus>): Collection<MembershipEntity>

}
