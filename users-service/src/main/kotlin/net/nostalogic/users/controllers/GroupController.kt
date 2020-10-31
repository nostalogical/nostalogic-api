package net.nostalogic.users.controllers

import net.nostalogic.users.UsersApplication
import net.nostalogic.users.controllers.GroupController.Companion.GROUPS_ENDPOINT
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(GROUPS_ENDPOINT, produces = ["application/json"])
class GroupController {

    companion object {
        const val GROUPS_ENDPOINT = "/v${UsersApplication.MAJOR}/groups"
    }


    // Get group by ID

    // Get groups - filterable

    // Create group

    // Update group

    // Delete group(s)
}
