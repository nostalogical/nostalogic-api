package net.nostalogic.access.persistence.repositories

import net.nostalogic.access.persistence.entities.PolicyEntity
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import javax.transaction.Transactional

interface PolicyRepository: JpaRepository<PolicyEntity, String> {

    fun findAllByIdInAndStatusIn(ids: Collection<String>, statuses: Collection<EntityStatus>, page: Pageable): Page<PolicyEntity>

    @Modifying
    @Transactional
    @Query("UPDATE policy SET status = :status WHERE id IN (:ids)")
    fun changePoliciesToStatus(ids: Collection<String>, status: EntityStatus)


    @Query("SELECT ps.policyId FROM policy_subject ps JOIN policy_resource pr ON pr.policyId = ps.policyId " +
            "WHERE (pr.resourceId IN (:resourceIds) OR pr.resourceId IS NULL AND (pr.entity IN (:resourceEntities) OR pr.entity = :allEntity)) AND (ps.entity = :allEntity OR ps.subjectId IN (:subjectIds))")
    fun findPolicyIdsForSubjectsAndResources(allEntity: NoEntity, resourceEntities: Collection<NoEntity>, resourceIds: Collection<String>, subjectIds: Collection<String>): Set<String>
}
