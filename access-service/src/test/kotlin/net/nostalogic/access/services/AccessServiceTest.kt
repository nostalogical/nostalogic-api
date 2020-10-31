package net.nostalogic.access.services

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.nostalogic.access.config.AccessTestConfig
import net.nostalogic.access.datamodel.PolicyEntityComponents
import net.nostalogic.access.mappers.PolicyMapper
import net.nostalogic.access.persistence.entities.PolicyActionEntity
import net.nostalogic.access.persistence.entities.PolicyEntity
import net.nostalogic.access.persistence.entities.PolicyResourceEntity
import net.nostalogic.access.persistence.entities.PolicySubjectEntity
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.datamodel.access.PolicyPriority
import net.nostalogic.entities.EntityStatus
import net.nostalogic.exceptions.NoValidationException
import net.nostalogic.utils.CollUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles(profiles = ["test"])
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AccessTestConfig::class])
class AccessServiceTest(
        @Autowired val accessService: AccessService
) : PolicyTestBase() {

    @BeforeEach
    fun setup() {
        clearAllMocks()
        every { policyRepository.save(ofType(PolicyEntity::class)) } answers{ firstArg() }
        every { actionRepository.save(ofType(PolicyActionEntity::class)) } answers{ firstArg() }
        every { resourceRepository.save(ofType(PolicyResourceEntity::class)) } answers{ firstArg() }
        every { subjectRepository.save(ofType(PolicySubjectEntity::class)) } answers{ firstArg() }
    }

    @Test
    fun `Create a policy`() {
        val policy = Policy(name = name, priority = PolicyPriority.TWO_STANDARD,
                resources = hashSetOf(resource.toEntityReference().toString()),
                subjects = hashSetOf(subject.toEntityReference().toString()),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        val result = accessService.createPolicy(policy)
        assertPoliciesEqual(policy, result, false)
    }

    @Test
    fun `Creating a policy with invalid fields should fail`() {
        val policy = Policy(name = "", priority = PolicyPriority.TWO_STANDARD,
                resources = HashSet(),
                subjects = HashSet(),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        Assertions.assertThrows(NoValidationException::class.java) { accessService.createPolicy(policy) }
    }

    @Test
    fun `Edit a policy`() {
        val policy = Policy(name = name, priority = PolicyPriority.TWO_STANDARD,
                resources = hashSetOf(resource.toEntityReference().toString()),
                subjects = hashSetOf(subject.toEntityReference().toString()),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        mockSavedPolicy(policy)
        val result = accessService.editPolicy(policy, policy.id!!)
        assertPoliciesEqual(policy, result)
        verify(exactly = 1) { policyRepository.save(ofType(PolicyEntity::class)) }
    }

    @Test
    fun `Delete a policy`() {
        val policy = Policy(name = name, priority = PolicyPriority.TWO_STANDARD,
                resources = hashSetOf(resource.toEntityReference().toString()),
                subjects = hashSetOf(subject.toEntityReference().toString()),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        mockSavedPolicy(policy)
        every { policyRepository.changePoliciesToStatus(any(), any()) } answers { Unit }
        accessService.deletePolicy(policy.id!!)
        verify(exactly = 1) { policyRepository.changePoliciesToStatus(setOf(policy.id!!), EntityStatus.DELETED) }
    }

    private fun mockSavedPolicy(vararg policies: Policy) {
        val entities: Collection<PolicyEntityComponents> = setOf(*policies).map { PolicyMapper.dtoToEntities(it) }
        val policyMock = mockk<Page<PolicyEntity>>()
        val policyIds = policies.map { p -> p.id!! }.toHashSet()
        entities.forEach {
            val policy = it.policy
            every { policyRepository.getOne(policy.id) } answers { policy } }
        every { policyRepository.findAllByIdInAndStatusIn(policyIds, any(), any()) } answers {policyMock}
        every { policyMock.hasNext() } answers { false }
        every { policyMock.iterator() } answers { entities.map { e -> e.policy }.toHashSet().iterator() }

        every { actionRepository.findAllByPolicyIdIn(policyIds) } answers { entities.flatMap { e -> e.actions }.toHashSet() }
        every { subjectRepository.findAllByPolicyIdIn(policyIds) } answers { entities.flatMap { e -> e.subjects }.toHashSet() }
        every { resourceRepository.findAllByPolicyIdIn(policyIds) } answers { entities.flatMap { e -> e.resources }.toHashSet() }
    }

}
