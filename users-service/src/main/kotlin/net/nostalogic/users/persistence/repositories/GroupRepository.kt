package net.nostalogic.users.persistence.repositories

import net.nostalogic.entities.EntityStatus
import net.nostalogic.users.constants.GroupType
import net.nostalogic.users.persistence.entities.GroupEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GroupRepository: JpaRepository<GroupEntity, String> {

    @Query(value = "SELECT count(*) = 0 FROM (SELECT 1 FROM \"group\" g WHERE g.name ilike ?1) groups;", nativeQuery = true)
    fun isGroupNameAvailable(name: String): Boolean

    fun findByNameEquals(name: String): GroupEntity?

    @Query(value = "UPDATE \"group\" SET status = :status WHERE id IN (:groupIds) RETURNING *", nativeQuery = true)
    fun updateGroupsStatus(groupIds: Collection<String>, status: String): Collection<GroupEntity>

    fun findAllByTypeInAndStatusIn(type: Collection<GroupType>, status: Collection<EntityStatus>, page: Pageable): Page<GroupEntity>
    fun findAllByIdInAndTypeInAndStatusIn(ids: Collection<String>, type: Collection<GroupType>, status: Collection<EntityStatus>, page: Pageable): Page<GroupEntity>

}
