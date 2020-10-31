package net.nostalogic.access.testutils

import net.nostalogic.datamodel.access.AccessReport
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.NoEntity
import org.junit.jupiter.api.Assertions

object TestUtils {

    @Suppress("DuplicatedCode", "MapGetWithNotNullAssertionOperator")
    fun assertReport(accessReport: AccessReport, subjects: HashSet<String>,
                             resources: HashMap<String, HashMap<PolicyAction, Boolean>>?,
                             entities: HashMap<NoEntity, HashMap<PolicyAction, Boolean>>?) {
        Assertions.assertEquals(subjects, accessReport.subjectIds)
        if (resources == null)
            Assertions.assertTrue(accessReport.resourcePermissions.isEmpty())
        else {
            for (resource in resources) {
                Assertions.assertTrue(accessReport.resourcePermissions.containsKey(resource.key))
                for (action in resource.value) {
                    Assertions.assertEquals(action.value, accessReport.resourcePermissions[resource.key]!![action.key])
                }
            }
        }
        if (entities == null)
            Assertions.assertTrue(accessReport.entityPermissions.isEmpty())
        else {
            for (entity in entities) {
                Assertions.assertTrue(accessReport.entityPermissions.containsKey(entity.key))
                for (action in entity.value) {
                    Assertions.assertEquals(action.value, accessReport.entityPermissions[entity.key]!![action.key])
                }
            }
        }
    }

}
