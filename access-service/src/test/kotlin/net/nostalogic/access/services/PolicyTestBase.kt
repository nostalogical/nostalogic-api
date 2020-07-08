package net.nostalogic.access.services

import net.nostalogic.access.persistence.repositories.PolicyActionRepository
import net.nostalogic.access.persistence.repositories.PolicyRepository
import net.nostalogic.access.persistence.repositories.PolicyResourceRepository
import net.nostalogic.access.persistence.repositories.PolicySubjectRepository
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntitySignature
import net.nostalogic.entities.NoEntity
import net.nostalogic.utils.EntityUtils
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

open class PolicyTestBase {


    @Autowired lateinit var policyRepository: PolicyRepository
    @Autowired lateinit var actionRepository: PolicyActionRepository
    @Autowired lateinit var resourceRepository: PolicyResourceRepository
    @Autowired lateinit var subjectRepository: PolicySubjectRepository

    val name = "Policy Test Name"
    val resource = EntitySignature(EntityUtils.uuid(), NoEntity.ARTICLE)
    val subject = EntitySignature(EntityUtils.uuid(), NoEntity.USER)

    fun enumMapOf(pair: Pair<PolicyAction, Boolean>): EnumMap<PolicyAction, Boolean> {
        val map: EnumMap<PolicyAction, Boolean> = EnumMap(PolicyAction::class.java)
        map[pair.first] = pair.second
        return map
    }

    fun isPoliciesEqual(expected: Policy, actual: Policy, id: Boolean = true, subjects: Boolean = true,
                        resources: Boolean = true, actions: Boolean = true): Boolean {
        var isEqual = expected.name.equals(actual.name)
                .and(expected.status == actual.status)
                .and(expected.priority == actual.priority)
        if (id)
            isEqual = isEqual && expected.id == actual.id
        if (subjects && isEqual) {
            var subjectsEqual = expected.subjects.size == actual.subjects.size
            for (sub in expected.subjects)
                subjectsEqual = subjectsEqual.and(actual.subjects.contains(sub))
            isEqual = subjectsEqual
        }
        if (resources && isEqual) {
            var resourcesEqual = expected.resources.size == actual.resources.size
            for (res in expected.resources)
                resourcesEqual = resourcesEqual.and(actual.resources.contains(res))
            isEqual = resourcesEqual
        }
        if (actions && isEqual) {
            var actionsEqual = expected.permissions.size == actual.permissions.size
            for (act in expected.permissions)
                actionsEqual = actionsEqual && actual.permissions.containsKey(act.key)
                        .and(actual.permissions[act.key] == act.value)
            isEqual = actionsEqual
        }
        return isEqual
    }

}
