package net.nostalogic.access.persistence.repositories

import net.nostalogic.access.persistence.entities.PolicyActionEntity
import org.springframework.data.repository.CrudRepository
import javax.transaction.Transactional

interface PolicyActionRepository: CrudRepository<PolicyActionEntity, String> {
    fun findAllByPolicyIdIn(policyIds: Collection<String>): Set<PolicyActionEntity>
    @Transactional
    fun deleteAllByIdIn(ids: Collection<String>)
}
