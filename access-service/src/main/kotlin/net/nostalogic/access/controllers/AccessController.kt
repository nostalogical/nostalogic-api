package net.nostalogic.access.controllers

import net.nostalogic.access.AccessApplication
import net.nostalogic.access.controllers.AccessController.Companion.ACCESS_ENDPOINT
import net.nostalogic.access.datamodel.PolicySearchCriteria
import net.nostalogic.access.datamodel.ResourcePermissionContext
import net.nostalogic.access.services.AccessQueryService
import net.nostalogic.access.services.AccessService
import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.datamodel.NoPageable
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.AccessReport
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.entities.EntityStatus
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ACCESS_ENDPOINT, produces = ["application/json"])
class AccessController(
        private val accessService: AccessService,
        private val queryService: AccessQueryService
) {

    companion object {
        const val ACCESS_ENDPOINT = "/v${AccessApplication.MAJOR}/access"
        const val POLICIES_URI = "/policies"
        const val ANALYSE_URI = "/analyse"
    }

    init {
        print("load")
    }

    @RequestMapping(method = [RequestMethod.POST], produces = ["application/json"])
    fun queryAccess(@RequestBody query: AccessQuery): AccessReport {
        return queryService.evaluateAccessQuery(query)
    }

    @RequestMapping(method = [RequestMethod.POST], produces = ["application/json"], path = [ANALYSE_URI])
    fun analyseAccess(@RequestBody query: AccessQuery): Collection<ResourcePermissionContext> {
        return queryService.analyseAccessQuery(query)
    }

    @RequestMapping(method = [RequestMethod.POST], produces = ["application/json"], path = [POLICIES_URI])
    @ResponseStatus(HttpStatus.CREATED)
    fun createPolicy(@RequestBody policy: Policy): Policy {
        return accessService.createPolicy(policy)
    }

    @RequestMapping(method = [RequestMethod.PUT], produces = ["application/json"], path = ["${POLICIES_URI}/{policyId}"])
    fun editPolicy(@RequestBody policy: Policy,
                   @PathVariable policyId: String): Policy {
        return accessService.editPolicy(policy, policyId)
    }

    @RequestMapping(method = [RequestMethod.DELETE], produces = ["application/json"], path = ["${POLICIES_URI}/{policyId}"])
    fun deletePolicy(@PathVariable policyId: String) {
        accessService.deletePolicy(policyId)
    }

    @RequestMapping(method = [RequestMethod.GET], produces = ["application/json"], path = ["${POLICIES_URI}/{policyId}"])
    fun getPolicy(@PathVariable policyId: String):Policy {
        return accessService.getPolicy(policyId)
    }

    @RequestMapping(method = [RequestMethod.GET], produces = ["application/json"], path = [POLICIES_URI])
    fun getPolicies(@RequestParam(defaultValue = "1") page: Int, @RequestParam(defaultValue = "20") size: Int,
                    @RequestParam subjects: Set<String>?,
                    @RequestParam resources: Set<String>?,
                    @RequestParam status: Set<EntityStatus>?): NoPageResponse<Policy> {
        val pageable = NoPageable<Policy>(page, size, *AccessQueryService.SORT_FIELDS)
        val result = accessService.getPolicies(PolicySearchCriteria(
                subjectIds = subjects, resourceIds =  resources, status = status, page = pageable))
        return pageable.toResponse(result)
    }

    @RequestMapping(method = [RequestMethod.GET], produces = ["application/json"], path = ["${POLICIES_URI}/search"])
    fun searchPolicies(@RequestParam(defaultValue = "1") page: Int, @RequestParam(defaultValue = "20") size: Int,
                       @RequestParam policies: Set<String>?,
                       @RequestParam subjects: Set<String>?,
                       @RequestParam resources: Set<String>?,
                       @RequestParam status: Set<EntityStatus>?): NoPageResponse<Policy> {
        val pageable = NoPageable<Policy>(page, size, *AccessQueryService.SORT_FIELDS)
        val result = queryService.searchPolicies(PolicySearchCriteria(
                policies, subjects, resources, status, pageable))
        return pageable.toResponse(result)
    }

}
