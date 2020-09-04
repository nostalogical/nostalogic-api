package net.nostalogic.access.services

import net.nostalogic.access.config.AccessTestConfig
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
        val policy = Policy(name = name, priority = PolicyPriority.LEVEL_TWO,
                resources = hashSetOf(resource.toFullId()),
                subjects = hashSetOf(subject.toFullId()),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        accessService.validatePolicy(policy)
    }

    @Test
    fun `Validate an entity resource level policy`() {
        val policy = Policy(name = name, priority = PolicyPriority.LEVEL_TWO,
                resources = hashSetOf(NoEntity.ARTICLE.name),
                subjects = hashSetOf(subject.toFullId()),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        accessService.validatePolicy(policy)
    }

    @Test
    fun `Empty resource policy should be invalid`() {
        val policy = Policy(name = name, priority = PolicyPriority.LEVEL_TWO,
                resources = hashSetOf(""),
                subjects = hashSetOf(subject.toFullId()),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        assertThrows<NoValidationException> { accessService.validatePolicy(policy) }
    }

    @Test
    fun `Entity level subject policy for not 'ALL' should be invalid`() {
        val policy = Policy(name = name, priority = PolicyPriority.LEVEL_TWO,
                resources = hashSetOf(NoEntity.ARTICLE.name),
                subjects = hashSetOf(NoEntity.USER.name),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        assertThrows<NoValidationException> { accessService.validatePolicy(policy) }
    }

    @Test
    fun `Entity level subject policy for 'ALL' should be valid`() {
        val policy = Policy(name = name, priority = PolicyPriority.LEVEL_TWO,
                resources = hashSetOf(NoEntity.ARTICLE.name),
                subjects = hashSetOf(NoEntity.ALL.name),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        accessService.validatePolicy(policy)
    }

    @Test
    fun `Empty resource list should be valid`() {
        val policy = Policy(name = name, priority = PolicyPriority.LEVEL_TWO,
                resources = HashSet(),
                subjects = hashSetOf(NoEntity.ALL.name),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        accessService.validatePolicy(policy)
        policy.resources = null
        accessService.validatePolicy(policy)
    }

    @Test
    fun `Empty or null subject list should be valid`() {
        val policy = Policy(name = name, priority = PolicyPriority.LEVEL_TWO,
                resources = hashSetOf(NoEntity.ARTICLE.name),
                subjects = HashSet(),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        accessService.validatePolicy(policy)
        policy.subjects = null
        accessService.validatePolicy(policy)
    }

    @Test
    fun `Improper UUID in the resource should be invalid`() {
        val policy = Policy(name = name, priority = PolicyPriority.LEVEL_TWO,
                resources = hashSetOf("not-a-uuid::" + NoEntity.ARTICLE),
                subjects = hashSetOf(NoEntity.ALL.name),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        assertThrows<NoValidationException> { accessService.validatePolicy(policy) }
    }

    @Test
    fun `Improper UUID in the subject should be invalid`() {
        val policy = Policy(name = name, priority = PolicyPriority.LEVEL_TWO,
                resources = hashSetOf(NoEntity.ARTICLE.name),
                subjects = hashSetOf("not-a-uuid::" + NoEntity.GROUP),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        assertThrows<NoValidationException> { accessService.validatePolicy(policy) }
    }

    @Test
    fun `An empty name should be invalid`() {
        val policy = Policy(name = "", priority = PolicyPriority.LEVEL_TWO,
                resources = hashSetOf(resource.toFullId()),
                subjects = hashSetOf(NoEntity.ALL.name),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        assertThrows<NoValidationException> { accessService.validatePolicy(policy) }
    }

    @Test
    fun `A long name should be invalid`() {
        val policy = Policy(name = "A name over the fifty character limit that the database would not accept",
                priority = PolicyPriority.LEVEL_TWO,
                resources = hashSetOf(resource.toFullId()),
                subjects = hashSetOf(subject.toFullId()),
                permissions = CollUtils.enumMapOf(Pair(PolicyAction.READ, true)))
        assertThrows<NoValidationException> { accessService.validatePolicy(policy) }
    }

    @Test
    fun `An empty permissions map should be valid`() {
        val policy = Policy(name = name, priority = PolicyPriority.LEVEL_TWO,
                resources = hashSetOf(resource.toFullId()),
                subjects = hashSetOf(subject.toFullId()))
        accessService.validatePolicy(policy)
        policy.resources = null
        accessService.validatePolicy(policy)
    }
}
