package net.nostalogic.access.services

import io.mockk.clearAllMocks
import io.mockk.every
import net.nostalogic.access.config.AccessTestConfig
import net.nostalogic.access.persistence.entities.PolicyActionEntity
import net.nostalogic.access.persistence.entities.PolicyEntity
import net.nostalogic.access.persistence.entities.PolicyResourceEntity
import net.nostalogic.access.persistence.entities.PolicySubjectEntity
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.datamodel.access.PolicyPriority
import net.nostalogic.exceptions.NoValidationException
import net.nostalogic.utils.CollUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
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
        val policy = Policy(name = name, priority = PolicyPriority.LEVEL_TWO,
                resources = hashSetOf(resource.toEntityReference()),
                subjects = hashSetOf(subject.toEntityReference()),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        val result = accessService.createPolicy(policy)
        assertPoliciesEqual(policy, result, false)
    }

    @Test
    fun `Creating a policy with invalid fields should fail`() {
        val policy = Policy(name = "", priority = PolicyPriority.LEVEL_TWO,
                resources = HashSet(),
                subjects = HashSet(),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        Assertions.assertThrows(NoValidationException::class.java) { accessService.createPolicy(policy) }
    }

//    @Test
//    fun `Edit a policy`() {
//        val policy = Policy(name = name, priority = PolicyPriority.LEVEL_TWO,
//                resources = hashSetOf(resource.toFullId()),
//                subjects = hashSetOf(subject.toFullId()),
//                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
//        val mock = mockk<Page<PolicyEntity>>()
//        every { policyRepository.findAllByIdInAndStatusIn(any(), any(), any()) } answers {mock}
//        every { mock.hasNext() } answers { false }
//        every { mock.iterator() } answers { hashSetOf(policy).iterator() }
//        val result = accessService.editPolicy(policy, EntityUtils.uuid())
//    }

//    private fun <T> mockPageResponse(entities: Collection<T>, callback: FunctionalInterface) {
//        val response = mockk<Page<T>>()
//
//        every { policyRepository.findAllByIdInAndStatusIn(any(), any(), any()) } answers {response}
//        every { policyRepository.findAllByIdInAndStatusIn(any(), any(), any()) } answers {response}
//    }

    // delete
}
