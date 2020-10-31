package net.nostalogic.users.controllers

import net.nostalogic.users.UsersApplication
import net.nostalogic.users.controllers.MembershipController.Companion.MEMBERS_ENDPOINT
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(MEMBERS_ENDPOINT, produces = ["application/json"])
class MembershipController {

    companion object {
        const val MEMBERS_ENDPOINT = "/v${UsersApplication.MAJOR}/members"
    }

    // Get groups for user

    // Get users for group

    // Add user(s) to group(s)

    // Remove user(s) from group(s)

    // Membership change summary? Included changes made, and a reasons for any failures (probably rights)

    // Activate membership

}
