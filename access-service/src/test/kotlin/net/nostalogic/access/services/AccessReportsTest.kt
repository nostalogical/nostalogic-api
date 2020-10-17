package net.nostalogic.access.services

import net.nostalogic.datamodel.access.AccessReport
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoAccessException
import net.nostalogic.utils.EntityUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*
import kotlin.collections.HashMap

@ActiveProfiles(profiles = ["test"])
@ExtendWith(SpringExtension::class)
class AccessReportsTest {

    private val subject = EntityUtils.uuid()
    private val testOne = EntityReference(EntityUtils.uuid(), NoEntity.ARTICLE)
    private val testTwo = EntityReference(EntityUtils.uuid(), NoEntity.GROUP)

    @Test
    fun `Resource level permission in the report works`() {
        val report = AccessReport(setOf(subject),
                Collections.singletonMap(testOne.toString(),
                        Collections.singletonMap(PolicyAction.READ, true)),
                Collections.singletonMap(testOne.entity,
                        Collections.singletonMap(PolicyAction.READ, true)))
        Assertions.assertTrue(report.hasPermission(testOne, PolicyAction.READ))
    }

    @Test
    fun `Entity level permission for a specific resource in the report works`() {
        val entityActions = HashMap<NoEntity, Map<PolicyAction, Boolean>>()
        entityActions[testOne.entity] = Collections.singletonMap(PolicyAction.CREATE, true)
        entityActions[testTwo.entity] = Collections.singletonMap(PolicyAction.DELETE, false)

        val report = AccessReport(setOf(subject),
                Collections.singletonMap(testOne.toString(),
                        Collections.singletonMap(PolicyAction.EDIT, true)),
                entityActions)
        Assertions.assertTrue(report.hasPermission(testOne, PolicyAction.CREATE))
        Assertions.assertFalse(report.hasPermission(testTwo, PolicyAction.DELETE))
    }

    @Test
    fun `Retrieving an unspecified permission from the report throws an error`() {
        val entityActions = HashMap<NoEntity, Map<PolicyAction, Boolean>>()
        entityActions[testOne.entity] = Collections.singletonMap(PolicyAction.CREATE, true)
        entityActions[testTwo.entity] = Collections.singletonMap(PolicyAction.DELETE, false)

        val report = AccessReport(setOf(subject),
                Collections.singletonMap(testOne.toString(),
                        Collections.singletonMap(PolicyAction.EDIT, true)),
                entityActions)
        assertThrows<NoAccessException> { report.hasPermission(testTwo, PolicyAction.EDIT) }
    }
}
