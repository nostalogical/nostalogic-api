package net.nostalogic.users.controllers

import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.datamodel.NoPageable
import net.nostalogic.entities.EntityStatus
import net.nostalogic.users.UsersApplication
import net.nostalogic.users.constants.GroupType
import net.nostalogic.users.controllers.GroupController.Companion.GROUPS_ENDPOINT
import net.nostalogic.users.datamodel.groups.Group
import net.nostalogic.users.datamodel.groups.GroupSearchCriteria
import net.nostalogic.users.services.GroupService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(GROUPS_ENDPOINT, produces = ["application/json"])
class GroupController(@Autowired private val groupService: GroupService) {

    companion object {
        const val GROUPS_ENDPOINT = "/v${UsersApplication.MAJOR}/groups"
    }

    @RequestMapping(method = [RequestMethod.POST])
    @ResponseStatus(HttpStatus.CREATED)
    fun createGroup(@RequestBody group: Group): Group {
        return groupService.createGroup(group)
    }

    @RequestMapping(method = [RequestMethod.PUT], path = ["/{groupId}"])
    fun updateGroup(@PathVariable groupId: String, @RequestBody group: Group): Group {
        return groupService.updateGroup(groupId, group)
    }

    @RequestMapping(method = [RequestMethod.DELETE], path = ["/{groupId}"])
    fun deleteGroup(@PathVariable groupId: String, @RequestParam hard: Boolean = false): Group {
        return groupService.deleteGroup(groupId, hard)
    }

    @RequestMapping(method = [RequestMethod.GET], path = ["/{groupId}"])
    fun getGroup(@PathVariable groupId: String): Group {
        return groupService.getGroup(groupId)
    }

    @RequestMapping(method = [RequestMethod.GET])
    fun searchGroups(@RequestParam(defaultValue = "1") page: Int,
                     @RequestParam(defaultValue = "20") size: Int,
                     @RequestParam id: Set<String>?,
                     @RequestParam user: Set<String>?,
                     @RequestParam type: Set<GroupType>?,
                     @RequestParam status: Set<EntityStatus>?): NoPageResponse<Group> {
        val pageable = NoPageable<Group>(page, size, *GroupSearchCriteria.DEFAULT_SORT_FIELDS)
        val result = groupService.getGroups(
                GroupSearchCriteria(groupIds = id, memberUserIds = user, type = type, status = status, page = pageable))
        return pageable.toResponse(result)
    }

}
