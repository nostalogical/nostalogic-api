package net.nostalogic.access.services

import net.nostalogic.datamodel.access.Policy
import net.nostalogic.entities.EntitySignature
import net.nostalogic.entities.NoEntity
import net.nostalogic.utils.EntityUtils
import org.junit.jupiter.api.Assertions

open class PolicyTestBase{

    val name = "Policy Test Name"
    val resource = EntitySignature(EntityUtils.uuid(), NoEntity.ARTICLE)
    val subject = EntitySignature(EntityUtils.uuid(), NoEntity.USER)

    fun assertPoliciesEqual(p1: Policy, p2:Policy, includeIds: Boolean = true) {
        if (includeIds)
            Assertions.assertEquals(p1, p2)
        else {
            Assertions.assertEquals(p1.name, p2.name)
            Assertions.assertEquals(p1.status, p2.status)
            Assertions.assertEquals(p1.priority, p2.priority)
            Assertions.assertEquals(p1.permissions, p2.permissions)
            Assertions.assertEquals(p1.resources, p2.resources)
            Assertions.assertEquals(p1.subjects, p2.subjects)
            Assertions.assertEquals(p1.creator, p2.creator)
        }
    }

}
