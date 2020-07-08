package net.nostalogic.access.controllers

import net.nostalogic.access.AccessApplication
import net.nostalogic.access.controllers.AccessController.Companion.ACCESS_ENDPOINT
import net.nostalogic.access.models.PolicySearchCriteria
import net.nostalogic.access.services.AccessQueryService
import net.nostalogic.access.services.AccessService
import net.nostalogic.constants.NoStrings
import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.datamodel.NoPageable
import net.nostalogic.datamodel.access.Policy
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(ACCESS_ENDPOINT)
class AccessController(
        private val accessService: AccessService
) {

    companion object {
        const val ACCESS_ENDPOINT = "/v${AccessApplication.MAJOR}/access"
    }

    @RequestMapping(method = [RequestMethod.POST], produces = ["application/json"])
    fun createPolicy(@RequestHeader(NoStrings.AUTH_HEADER) token: String, @RequestBody policy: Policy): Policy {
        return accessService.createPolicy(policy)
    }

    @RequestMapping(method = [RequestMethod.PUT], produces = ["application/json"], path = ["/{policyId}"])
    fun editPolicy(@RequestHeader(NoStrings.AUTH_HEADER) token: String, @RequestBody policy: Policy,
                   @PathVariable policyId: String): Policy {
        return accessService.editPolicy(policy, policyId)
    }

    @RequestMapping(method = [RequestMethod.DELETE], produces = ["application/json"], path = ["/{policyId}"])
    fun deletePolicy(@RequestHeader(NoStrings.AUTH_HEADER) token: String, @PathVariable policyId: String) {
        accessService.deletePolicy(policyId)
    }

    @RequestMapping(method = [RequestMethod.GET], produces = ["application/json"])
    fun searchPolicies(@RequestHeader(NoStrings.AUTH_HEADER) token: String,
                       @RequestParam(defaultValue = "1") page: Int, @RequestParam(defaultValue = "20") size: Int,
                       @RequestParam policies: Set<String>?,
                       @RequestParam subjects: Set<String>?,
                       @RequestParam resources: Set<String>?): NoPageResponse<Policy> {
        val pageable = NoPageable<Policy>(page, size, *AccessQueryService.SEARCH_PROPS)
        val result = accessService.searchPolicies(PolicySearchCriteria(
                policies ?: Collections.emptySet(),
                subjects ?: Collections.emptySet(),
                resources ?: Collections.emptySet(),
                pageable))
        return pageable.toResponse(result)
    }

}
