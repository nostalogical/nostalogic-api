package net.nostalogic.users.persistence.repositories

import net.nostalogic.users.persistence.entities.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository: JpaRepository<UserEntity, String> {

    @Query(value = "SELECT count(*) = 0 FROM (SELECT 1 FROM \"user\" u WHERE u.email ilike ?1) users;", nativeQuery = true)
    fun isEmailAvailable(email: String): Boolean

    @Query(value = "SELECT count(*) = 0 FROM (SELECT 1 FROM \"user\" u WHERE u.username ilike ?1) users;", nativeQuery = true)
    fun isUsernameAvailable(email: String): Boolean

    @Query(value = "UPDATE \"user\" SET status = :status WHERE id IN (:userIds) RETURNING *", nativeQuery = true)
    fun updateUsersStatus(userIds: Collection<String>, status: String): Collection<UserEntity>

    @Query(value = "SELECT u.* FROM \"user\" u WHERE (u.id IN (:userIds) OR u.username IN (:usernames) OR u.email IN (:emails)) AND u.status IN (:status)", nativeQuery = true)
    fun searchUsersByIdentifiers(userIds: Collection<String>, usernames: Collection<String>, emails: Collection<String>, status: Collection<String>, page: Pageable): Page<UserEntity>

    @Query(value = "SELECT u.* FROM \"user\" u JOIN membership m ON m.user_id = u.id WHERE m.group_id IN (:groupIds) AND (u.id IN (:userIds) OR u.username IN (:usernames) OR u.email IN (:emails)) AND u.status IN (:status)", nativeQuery = true)
    fun searchUsersByIdentifiersAndGroups(userIds: Collection<String>, groupIds: Collection<String>, usernames: Collection<String>, emails: Collection<String>, status: Collection<String>, page: Pageable): Page<UserEntity>

    @Query(value = "SELECT u.* FROM \"user\" u JOIN membership m ON m.user_id = u.id WHERE m.group_id IN (:groupIds) AND u.status IN (:status)", nativeQuery = true)
    fun searchUsersByGroups(groupIds: Collection<String>, status: Collection<String>, page: Pageable): Page<UserEntity>

    @Query(value = "SELECT u.* FROM \"user\" u WHERE u.status IN (:status)", nativeQuery = true)
    fun searchUsers(status: Collection<String>, page: Pageable): Page<UserEntity>

    fun findByUsernameEquals(name: String): UserEntity?

    fun findByEmailEquals(email: String): UserEntity?

    fun findByIdEquals(id: String): UserEntity?

}
