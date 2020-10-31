package net.nostalogic.users.persistence.repositories

import net.nostalogic.entities.EntityStatus
import net.nostalogic.users.persistence.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository: JpaRepository<UserEntity, String> {

    @Query(value = "SELECT count(*) = 0 FROM (SELECT 1 FROM \"user\" u WHERE u.email ilike ?1) users;", nativeQuery = true)
    fun isEmailAvailable(email: String): Boolean

    @Query(value = "SELECT count(*) = 0 FROM (SELECT 1 FROM \"user\" u WHERE u.name ilike ?1) users;", nativeQuery = true)
    fun isUsernameAvailable(email: String): Boolean

    @Query(value = "UPDATE \"user\" SET status = ?1 WHERE id IN (?2)", nativeQuery = true)
    fun updateUsersStatus(userIds: Collection<String>, status: EntityStatus)

    fun findByNameEquals(name: String): UserEntity?

    fun findByEmailEquals(email: String): UserEntity?

    fun findByIdEquals(id: String): UserEntity?

}
