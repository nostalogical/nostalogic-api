package net.nostalogic.access.models

import net.nostalogic.access.services.AccessQueryService
import net.nostalogic.datamodel.NoPageable
import net.nostalogic.datamodel.access.Policy
import java.util.*

class PolicySearchCriteria(
        val policyIds: Collection<String> = Collections.emptySet(),
        val subjectIds: Collection<String> = Collections.emptySet(),
        val resourceIds: Collection<String> = Collections.emptySet(),
        val page: NoPageable<Policy> = NoPageable(1, 20, *AccessQueryService.SEARCH_PROPS)
)
