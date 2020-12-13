package net.nostalogic.access.datamodel

import net.nostalogic.datamodel.NoPageable
import net.nostalogic.datamodel.SearchCriteria
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.datamodel.access.PolicyPriority
import net.nostalogic.entities.EntityStatus
import java.util.*

class PolicySearchCriteria(policyIds: Collection<String>? = null, subjectIds: Collection<String>? = null,
                           resourceIds: Collection<String>? = null, status: Collection<EntityStatus>? = null,
                           page: NoPageable<Policy>? = null, priority: Set<PolicyPriority>? = null,
                           actions: Set<PolicyAction>? = null): SearchCriteria<Policy> {

    companion object {
        val DEFAULT_SORT_FIELDS = arrayOf("created", "id")
    }

    val policyIds: Collection<String> = policyIds ?: Collections.emptySet()
    val subjectIds: Collection<String> = subjectIds ?: Collections.emptySet()
    val resourceIds: Collection<String> = resourceIds ?: Collections.emptySet()
    val status: Collection<EntityStatus> = status ?: setOf(EntityStatus.ACTIVE)
    val priority: Collection<PolicyPriority> = if (!priority.isNullOrEmpty()) priority else PolicyPriority.values().toHashSet()
    val actions: Collection<PolicyAction> = if (!actions.isNullOrEmpty()) actions else PolicyAction.values().toHashSet()
    val page: NoPageable<Policy> = page ?: NoPageable(1, 20, *DEFAULT_SORT_FIELDS)
}
