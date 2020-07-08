package net.nostalogic.access.persistence.repositories

import net.nostalogic.access.persistence.entities.PolicyResourceEntity
import net.nostalogic.entities.NoEntity
import org.springframework.data.repository.CrudRepository
import javax.transaction.Transactional

interface PolicyResourceRepository: CrudRepository<PolicyResourceEntity, String> {
    fun findAllByResourceIdIn(resourceIds: Collection<String>): Set<PolicyResourceEntity>
    fun findAllByEntityIn(entities: Collection<NoEntity>): Set<PolicyResourceEntity>
    fun findAllByPolicyIdIn(policyIds: Collection<String>): Set<PolicyResourceEntity>
    @Transactional
    fun deleteAllByIdIn(ids: Collection<String>)
}
