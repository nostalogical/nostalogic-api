package net.nostalogic.access.services

import net.nostalogic.access.config.AccessTestConfig
import net.nostalogic.access.validators.PolicyValidator
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.datamodel.access.PolicyPriority
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoValidationException
import net.nostalogic.utils.CollUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles(profiles = ["test"])
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AccessTestConfig::class])
class PolicyValidationTest(
        @Autowired val accessService: AccessService) : PolicyTestBase() {

    @BeforeEach
    fun setup() {

    }

    @Test
    fun `Validate a standard policy`() {
        val policy = Policy(name = name, priority = PolicyPriority.TWO_STANDARD,
                resources = hashSetOf(resource.toEntityReference().toString()),
                subjects = hashSetOf(subject.toEntityReference().toString()),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        PolicyValidator.validate(policy)
    }

    @Test
    fun `Validate an entity resource level policy`() {
        val policy = Policy(name = name, priority = PolicyPriority.TWO_STANDARD,
                resources = hashSetOf(NoEntity.ARTICLE.name),
                subjects = hashSetOf(subject.toEntityReference().toString()),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        PolicyValidator.validate(policy)
    }

    @Test
    fun `Empty resource policy should be invalid`() {
        val policy = Policy(name = name, priority = PolicyPriority.TWO_STANDARD,
                resources = hashSetOf(""),
                subjects = hashSetOf(subject.toEntityReference().toString()),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        assertThrows<NoValidationException> { PolicyValidator.validate(policy) }
    }

    @Test
    fun `Entity level subject policy for not 'ALL' should be invalid`() {
        val policy = Policy(name = name, priority = PolicyPriority.TWO_STANDARD,
                resources = hashSetOf(NoEntity.ARTICLE.name),
                subjects = hashSetOf(NoEntity.USER.name),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        assertThrows<NoValidationException> { PolicyValidator.validate(policy) }
    }

    @Test
    fun `Entity level subject policy for 'ALL' should be valid`() {
        val policy = Policy(name = name, priority = PolicyPriority.TWO_STANDARD,
                resources = hashSetOf(NoEntity.ARTICLE.name),
                subjects = hashSetOf(NoEntity.ALL.name),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        PolicyValidator.validate(policy)
    }

    @Test
    fun `Empty resource list should be valid`() {
        val policy = Policy(name = name, priority = PolicyPriority.TWO_STANDARD,
                resources = HashSet(),
                subjects = hashSetOf(NoEntity.ALL.name),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        PolicyValidator.validate(policy)
        policy.resources = null
        PolicyValidator.validate(policy)
    }

    @Test
    fun `Empty or null subject list should be valid`() {
        val policy = Policy(name = name, priority = PolicyPriority.TWO_STANDARD,
                resources = hashSetOf(NoEntity.ARTICLE.name),
                subjects = HashSet(),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        PolicyValidator.validate(policy)
        policy.subjects = null
        PolicyValidator.validate(policy)
    }

    @Test
    fun `Improper UUID in the resource should be invalid`() {
        val policy = Policy(name = name, priority = PolicyPriority.TWO_STANDARD,
                resources = hashSetOf("not-a-uuid::" + NoEntity.ARTICLE),
                subjects = hashSetOf(NoEntity.ALL.name),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        assertThrows<NoValidationException> { PolicyValidator.validate(policy) }
    }

    @Test
    fun `Improper UUID in the subject should be invalid`() {
        val policy = Policy(name = name, priority = PolicyPriority.TWO_STANDARD,
                resources = hashSetOf(NoEntity.ARTICLE.name),
                subjects = hashSetOf("not-a-uuid::" + NoEntity.GROUP),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        assertThrows<NoValidationException> { PolicyValidator.validate(policy) }
    }

    @Test
    fun `An empty name should be invalid`() {
        val policy = Policy(name = "", priority = PolicyPriority.TWO_STANDARD,
                resources = hashSetOf(resource.toEntityReference().toString()),
                subjects = hashSetOf(NoEntity.ALL.name),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        assertThrows<NoValidationException> { PolicyValidator.validate(policy) }
    }

    @Test
    fun `An empty name should be valid for an existing policy`() {
        val policy = Policy(name = "", priority = PolicyPriority.TWO_STANDARD,
                resources = hashSetOf(resource.toEntityReference().toString()),
                subjects = hashSetOf(NoEntity.ALL.name),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        PolicyValidator.validate(policy, false)
    }

    @Test
    fun `An null priority should be valid for an existing policy`() {
        val policy = Policy(name = name, priority = null,
                resources = hashSetOf(resource.toEntityReference().toString()),
                subjects = hashSetOf(NoEntity.ALL.name),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        PolicyValidator.validate(policy, false)
    }

    @Test
    fun `A long name should be invalid`() {
        val policy = Policy(name = "A long name over the one hundred character limit which should throw an exception unless I've increased the name limit",
                priority = PolicyPriority.TWO_STANDARD,
                resources = hashSetOf(resource.toEntityReference().toString()),
                subjects = hashSetOf(subject.toEntityReference().toString()),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        assertThrows<NoValidationException> { PolicyValidator.validate(policy) }
    }

    @Test
    fun `An empty permissions map should be valid`() {
        val policy = Policy(name = name, priority = PolicyPriority.TWO_STANDARD,
                resources = hashSetOf(resource.toEntityReference().toString()),
                subjects = hashSetOf(subject.toEntityReference().toString()))
        PolicyValidator.validate(policy)
        policy.resources = null
        PolicyValidator.validate(policy)
    }
}
