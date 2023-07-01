package net.nostalogic.access.testutils

import net.nostalogic.datamodel.access.AccessReport
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.NoEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

object TestUtils {

    @Suppress("DuplicatedCode", "MapGetWithNotNullAssertionOperator")
    fun assertReport(accessReport: AccessReport, subjects: HashSet<String>,
                             resources: HashMap<String, HashMap<PolicyAction, Boolean>>?,
                             entities: HashMap<NoEntity, HashMap<PolicyAction, Boolean>>?) {
        assertEquals(subjects, accessReport.subjectIds)
        if (resources == null)
            assertTrue(accessReport.resourcePermissions.isEmpty())
        else {
            for (resource in resources) {
                assertTrue(accessReport.resourcePermissions.containsKey(resource.key))
                for (action in resource.value) {
                    assertEquals(action.value, accessReport.resourcePermissions[resource.key]!![action.key])
                }
            }
        }
        if (entities == null)
            assertTrue(accessReport.entityPermissions.isEmpty())
        else {
            for (entity in entities) {
                assertTrue(accessReport.entityPermissions.containsKey(entity.key))
                for (action in entity.value) {
                    assertEquals(action.value, accessReport.entityPermissions[entity.key]!![action.key])
                }
            }
        }
    }

    fun assertPoliciesEqual(p1: Policy, p2: Policy, includeIds: Boolean = true) {
        if (includeIds)
            assertEquals(p1, p2)
        else {
            assertEquals(p1.name, p2.name)
            assertEquals(p1.status, p2.status)
            assertEquals(p1.priority, p2.priority)
            assertEquals(p1.permissions, p2.permissions)
            assertEquals(p1.resources, p2.resources)
            assertEquals(p1.subjects, p2.subjects)
            assertEquals(p1.creator, p2.creator)
        }
    }

}
