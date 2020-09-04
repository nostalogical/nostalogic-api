package net.nostalogic.access.persistence.repositories

import net.nostalogic.access.persistence.entities.PolicyEntity
import net.nostalogic.entities.EntityStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface PolicyRepository: JpaRepository<PolicyEntity, String> {

    fun findAllByIdInAndStatusIn(ids: Collection<String>, statuses: Collection<EntityStatus>, page: Pageable): Page<PolicyEntity>

    @Modifying
    @Query("UPDATE policy SET status = :status WHERE id IN (:ids)")
    fun changePoliciesToStatus(ids: Collection<String>, status: EntityStatus)
}
