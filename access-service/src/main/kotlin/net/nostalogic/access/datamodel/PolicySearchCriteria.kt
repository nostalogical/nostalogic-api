package net.nostalogic.access.datamodel

import net.nostalogic.access.services.AccessQueryService
import net.nostalogic.datamodel.NoPageable
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.entities.EntityStatus
import java.util.*

class PolicySearchCriteria(policyIds: Collection<String>? = null, subjectIds: Collection<String>? = null,
                           resourceIds: Collection<String>? = null, status: Collection<EntityStatus>? = null,
                           page: NoPageable<Policy>? = null) {
    val policyIds: Collection<String> = policyIds ?: Collections.emptySet()
    val subjectIds: Collection<String> = subjectIds ?: Collections.emptySet()
    val resourceIds: Collection<String> = resourceIds ?: Collections.emptySet()
    val status: Collection<EntityStatus> = status ?: setOf(EntityStatus.ACTIVE)
    val page: NoPageable<Policy> = page ?: NoPageable(1, 20, *AccessQueryService.SEARCH_PROPS)
}
