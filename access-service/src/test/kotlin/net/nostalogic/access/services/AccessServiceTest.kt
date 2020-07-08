package net.nostalogic.access.services

import io.mockk.clearAllMocks
import io.mockk.every
import net.nostalogic.access.config.AccessTestConfig
import net.nostalogic.access.persistence.entities.PolicyActionEntity
import net.nostalogic.access.persistence.entities.PolicyEntity
import net.nostalogic.access.persistence.entities.PolicyResourceEntity
import net.nostalogic.access.persistence.entities.PolicySubjectEntity
import net.nostalogic.access.persistence.repositories.PolicyActionRepository
import net.nostalogic.access.persistence.repositories.PolicyRepository
import net.nostalogic.access.persistence.repositories.PolicyResourceRepository
import net.nostalogic.access.persistence.repositories.PolicySubjectRepository
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.datamodel.access.PolicyPriority
import net.nostalogic.exceptions.NoValidationException
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
                resources = hashSetOf(resource.toFullId()),
                subjects = hashSetOf(subject.toFullId()),
                permissions = enumMapOf(Pair(PolicyAction.READ, true)))
        val result = accessService.createPolicy(policy)
        Assertions.assertTrue(isPoliciesEqual(policy, result, id = false))
    }

    @Test
    fun `Creating a policy with invalid fields should fail`() {
        val policy = Policy(name = "", priority = PolicyPriority.LEVEL_TWO,
                resources = HashSet(),
                subjects = HashSet(),
                permissions = enumMapOf(Pair(PolicyAction.READ, true)))
        Assertions.assertThrows(NoValidationException::class.java) { accessService.createPolicy(policy) }
    }
}
