package net.nostalogic.users.persistence.repositories

import net.nostalogic.users.constants.MembershipStatus
import net.nostalogic.users.persistence.entities.MembershipEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface MembershipRepository: JpaRepository<MembershipEntity, String> {

    fun findAllByUserIdEqualsAndStatusIn(
        userId: String,
        status: Collection<MembershipStatus>
    ): Collection<MembershipEntity>

    fun findAllByUserIdInAndStatusIn(
        userIds: Collection<String>,
        status: Collection<MembershipStatus>
    ): Collection<MembershipEntity>

    @Query(value = "SELECT m.*, g.\"name\", g.type, g.rights, u.username, u.username FROM membership m JOIN \"group\" g ON g.id = m.group_id JOIN \"user\" u ON u.id = m.user_id WHERE g.type IN (:groupType) AND g.rights IN (:rightsGroup) AND m.status IN (:status) AND u.status != 'DELETED' AND g.status != 'DELETED'", nativeQuery = true)
    fun searchMemberships(
        groupType: Collection<String>,
        rightsGroup: Collection<Boolean>,
        status: Collection<String>,
        page: Pageable
    ): Page<MembershipEntity>

    @Query(value = "SELECT m.*, g.\"name\", g.type, g.rights, u.username, u.username FROM membership m JOIN \"group\" g ON g.id = m.group_id JOIN \"user\" u ON u.id = m.user_id WHERE g.type IN (:groupType) AND g.rights IN (:rightsGroup) AND m.status IN (:status) AND g.id IN (:groupIds) AND u.status != 'DELETED' AND g.status != 'DELETED'", nativeQuery = true)
    fun searchMembershipsForGroups(
        groupIds: Collection<String>,
        groupType: Collection<String>,
        rightsGroup: Collection<Boolean>,
        status: Collection<String>,
        page: Pageable
    ): Page<MembershipEntity>

    @Query(value = "SELECT m.*, g.\"name\", g.type, g.rights, u.username, u.username FROM membership m JOIN \"group\" g ON g.id = m.group_id JOIN \"user\" u ON u.id = m.user_id WHERE g.type IN (:groupType) AND g.rights IN (:rightsGroup) AND m.status IN (:status) AND u.id IN (:userIds) AND u.status != 'DELETED' AND g.status != 'DELETED'", nativeQuery = true)
    fun searchMembershipsForUsers(
        userIds: Collection<String>,
        groupType: Collection<String>,
        rightsGroup: Collection<Boolean>,
        status: Collection<String>,
        page: Pageable
    ): Page<MembershipEntity>

    @Query(value = "SELECT m.*, g.\"name\", g.type, g.rights, u.username, u.username FROM membership m JOIN \"group\" g ON g.id = m.group_id JOIN \"user\" u ON u.id = m.user_id WHERE g.type IN (:groupType) AND g.rights IN (:rightsGroup) AND m.status IN (:status) AND u.id IN (:userIds) AND g.id IN (:groupIds) AND u.status != 'DELETED' AND g.status != 'DELETED'", nativeQuery = true)
    fun searchMembershipsForUsersAndGroups(
        userIds: Collection<String>,
        groupIds: Collection<String>,
        groupType: Collection<String>,
        rightsGroup: Collection<Boolean>,
        status: Collection<String>,
        page: Pageable
    ): Page<MembershipEntity>

    @Query(value = "SELECT m.*, g.\"name\", g.type, g.rights, u.username, u.username FROM membership m JOIN \"group\" g ON g.id = m.group_id JOIN \"user\" u ON u.id = m.user_id WHERE u.id IN (:userIds) AND g.id IN (:groupIds) AND u.status != 'DELETED' AND g.status != 'DELETED'", nativeQuery = true)
    fun findAllByUserIdInAndGroupIdIn(
        userIds: Collection<String>,
        groupIds: Collection<String>
    ): Collection<MembershipEntity>

    @Query(value = "SELECT m.*, g.\"name\", g.type, g.rights, u.username, u.username FROM membership m JOIN \"group\" g ON g.id = m.group_id JOIN \"user\" u ON u.id = m.user_id WHERE u.id IN (:userIds) AND g.status = 'ACTIVE'", nativeQuery = true)
    fun getActiveGroupsForUsers(
        userIds: Collection<String>
    ): Collection<MembershipEntity>

    @Transactional
    fun deleteAllByUserId(userId: String)
    @Transactional
    fun deleteAllByGroupId(groupId: String)

}
