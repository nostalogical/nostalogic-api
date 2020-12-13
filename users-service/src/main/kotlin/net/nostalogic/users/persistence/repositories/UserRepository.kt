package net.nostalogic.users.persistence.repositories

import net.nostalogic.entities.EntityStatus
import net.nostalogic.users.persistence.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository: JpaRepository<UserEntity, String> {

    @Query(value = "SELECT count(*) = 0 FROM (SELECT 1 FROM \"user\" u WHERE u.email ilike ?1) users;", nativeQuery = true)
    fun isEmailAvailable(email: String): Boolean

    @Query(value = "SELECT count(*) = 0 FROM (SELECT 1 FROM \"user\" u WHERE u.username ilike ?1) users;", nativeQuery = true)
    fun isUsernameAvailable(email: String): Boolean

    @Query(value = "UPDATE \"user\" SET status = :status WHERE id IN (:userIds) RETURNING *", nativeQuery = true)
    fun updateUsersStatus(userIds: Collection<String>, status: String): Collection<UserEntity>

    fun findByUsernameInOrEmailIn(names: Collection<String>, emails: Collection<String>): Collection<UserEntity>

    fun findByUsernameEquals(name: String): UserEntity?

    fun findByEmailEquals(email: String): UserEntity?

    fun findByIdEquals(id: String): UserEntity?

}
