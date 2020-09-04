package net.nostalogic.access.services

import net.nostalogic.access.persistence.repositories.PolicyActionRepository
import net.nostalogic.access.persistence.repositories.PolicyRepository
import net.nostalogic.access.persistence.repositories.PolicyResourceRepository
import net.nostalogic.access.persistence.repositories.PolicySubjectRepository
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.entities.EntitySignature
import net.nostalogic.entities.NoEntity
import net.nostalogic.utils.EntityUtils
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired

open class PolicyTestBase {

    @Autowired lateinit var policyRepository: PolicyRepository
    @Autowired lateinit var actionRepository: PolicyActionRepository
    @Autowired lateinit var resourceRepository: PolicyResourceRepository
    @Autowired lateinit var subjectRepository: PolicySubjectRepository

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
        }
    }

}
