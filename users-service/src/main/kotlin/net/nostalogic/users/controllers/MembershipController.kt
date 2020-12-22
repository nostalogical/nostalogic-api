package net.nostalogic.users.controllers

import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.datamodel.NoPageable
import net.nostalogic.users.UsersApplication
import net.nostalogic.users.constants.GroupType
import net.nostalogic.users.constants.MembershipStatus
import net.nostalogic.users.controllers.MembershipController.Companion.MEMBERS_ENDPOINT
import net.nostalogic.users.datamodel.memberships.GroupMembershipChanges
import net.nostalogic.users.datamodel.memberships.Membership
import net.nostalogic.users.datamodel.memberships.MembershipSearchCriteria
import net.nostalogic.users.services.MembershipService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping(MEMBERS_ENDPOINT, produces = ["application/json"])
class MembershipController(@Autowired private val membershipService: MembershipService) {

    companion object {
        const val MEMBERS_ENDPOINT = "/api/v${UsersApplication.MAJOR}/members"
        const val USERS_ENDPOINT = "/users"
        const val GROUPS_ENDPOINT = "/groups"
    }

    @RequestMapping(method = [RequestMethod.GET])
    fun getMemberships(@RequestParam(defaultValue = "1") page: Int,
                       @RequestParam(defaultValue = "20") size: Int,
                       @RequestParam userId: Set<String>?,
                       @RequestParam groupId: Set<String>?,
                       @RequestParam type: Set<GroupType>?,
                       @RequestParam status: Set<MembershipStatus>?): NoPageResponse<Membership> {
        val pageable = NoPageable<Membership>(page, size, *MembershipSearchCriteria.BULK_SORT_FIELDS)
        val result = membershipService.getMemberships(
                MembershipSearchCriteria(userIds = userId, groupIds = groupId, type = type, status = status, page = pageable))
        return pageable.toResponse(result)
    }

    @RequestMapping(method = [RequestMethod.GET], path = ["$USERS_ENDPOINT/{userId}"])
    fun getUserMemberships(@RequestParam(defaultValue = "1") page: Int,
                           @RequestParam(defaultValue = "50") size: Int,
                           @PathVariable userId: String): NoPageResponse<Membership> {
        val pageable = NoPageable<Membership>(page, size, *MembershipSearchCriteria.SINGLE_SORT_FIELDS)
        val result = membershipService.getMemberships(
                MembershipSearchCriteria(userIds = setOf(userId), page = pageable), showUsers = false)
        return pageable.toResponse(result)
    }

    @RequestMapping(method = [RequestMethod.GET], path = ["$GROUPS_ENDPOINT/{groupId}"])
    fun getGroupMemberships(@RequestParam(defaultValue = "1") page: Int,
                            @RequestParam(defaultValue = "50") size: Int,
                            @PathVariable groupId: String): NoPageResponse<Membership> {
        val pageable = NoPageable<Membership>(page, size, *MembershipSearchCriteria.SINGLE_SORT_FIELDS)
        val result = membershipService.getMemberships(
                MembershipSearchCriteria(groupIds = setOf(groupId), page = pageable), showGroups = false)
        return pageable.toResponse(result)
    }

    @RequestMapping(method = [RequestMethod.PUT], path = ["$GROUPS_ENDPOINT/{groupId}"])
    fun addUsersToGroup(@PathVariable groupId: String, @RequestBody userIds: HashSet<String>): List<GroupMembershipChanges> {
        return membershipService.addUsersToGroups(userIds, setOf(groupId))
    }

    @RequestMapping(method = [RequestMethod.PUT], path = ["$USERS_ENDPOINT/{userId}"])
    fun addUserToGroups(@PathVariable userId: String, @RequestBody groupIds: HashSet<String>): List<GroupMembershipChanges> {
        return membershipService.addUsersToGroups(setOf(userId), groupIds)
    }

    @RequestMapping(method = [RequestMethod.DELETE], path = ["$GROUPS_ENDPOINT/{groupId}"])
    fun removeUsersFromGroup(@PathVariable groupId: String, @RequestBody userIds: HashSet<String>): List<GroupMembershipChanges> {
        return membershipService.removeUsersFromGroups(userIds, setOf(groupId))
    }

    @RequestMapping(method = [RequestMethod.DELETE], path = ["$USERS_ENDPOINT/{userId}"])
    fun removeUserFromGroups(@PathVariable userId: String, @RequestBody groupIds: HashSet<String>): List<GroupMembershipChanges> {
        return membershipService.removeUsersFromGroups(setOf(userId), groupIds)
    }

    @RequestMapping(method = [RequestMethod.PUT], path = ["$GROUPS_ENDPOINT/{groupId}/$USERS_ENDPOINT/{userId}"])
    fun updateMembership(@PathVariable userId: String, @PathVariable groupId: String, @RequestBody update: Membership): Membership {
        return membershipService.updateMembership(update, userId, groupId)
    }
}
