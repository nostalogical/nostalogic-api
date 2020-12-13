package net.nostalogic.access.persistence.repositories

import net.nostalogic.access.persistence.entities.PolicyActionEntity
import net.nostalogic.datamodel.access.PolicyAction
import org.springframework.data.repository.CrudRepository
import javax.transaction.Transactional

interface PolicyActionRepository: CrudRepository<PolicyActionEntity, String> {
    fun findAllByActionIn(actions: Collection<PolicyAction>): Set<PolicyActionEntity>
    fun findAllByPolicyIdIn(policyIds: Collection<String>): Set<PolicyActionEntity>
    @Transactional
    fun deleteAllByIdIn(ids: Collection<String>)
}
