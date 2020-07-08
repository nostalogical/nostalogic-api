package net.nostalogic.access.persistence.repositories

import net.nostalogic.access.persistence.entities.PolicySubjectEntity
import org.springframework.data.repository.CrudRepository
import javax.transaction.Transactional

interface PolicySubjectRepository: CrudRepository<PolicySubjectEntity, String> {
    fun findAllBySubjectIdIn(subjectIds: Collection<String>): Set<PolicySubjectEntity>
    fun findAllBySubjectIdIsNull(): Set<PolicySubjectEntity>
    fun findAllByPolicyIdIn(policyIds: Collection<String>): Set<PolicySubjectEntity>
    @Transactional
    fun deleteAllByIdIn(ids: Collection<String>)
}
