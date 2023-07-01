package net.nostalogic.users.controllers

import net.nostalogic.constants.NoStrings
import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.datamodel.NoPageable
import net.nostalogic.entities.EntityStatus
import net.nostalogic.security.contexts.SessionContext
import net.nostalogic.users.UsersApplication
import net.nostalogic.users.controllers.UserController.Companion.USERS_ENDPOINT
import net.nostalogic.users.datamodel.users.*
import net.nostalogic.users.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping(USERS_ENDPOINT, produces = ["application/json"])
class UserController(@Autowired val userService: UserService) {

    companion object {
        const val USERS_ENDPOINT = "/api/v${UsersApplication.MAJOR}/users"
        const val REGISTER_URI = "/register"
        const val AVAILABLE_URI = "/check"
        const val CONFIRM_URI = "/confirm"
        const val PROFILE_URI = "/profile"
        const val SECURE_URI = "/secure"
    }

    @RequestMapping(method = [RequestMethod.POST])
    fun createUser(@RequestBody userRegistration: UserRegistration): User {
        SessionContext.requireLogin()
        return userService.createUser(userRegistration)
    }

    // TODO: Probably no longer want to return the user here
    @RequestMapping(method = [RequestMethod.POST], path = [REGISTER_URI])
    fun register(@RequestBody userRegistration: UserRegistration): RegistrationResponse {
        return userService.registerUser(userRegistration)
    }

    // TODO: Completely deprecate, no longer want to expose this
    @RequestMapping(method = [RequestMethod.POST], path = [REGISTER_URI + AVAILABLE_URI])
    fun availableCheck(@RequestBody userRegistration: UserRegistration): RegistrationAvailability {
        return userService.checkRegistrationAvailable(userRegistration)
    }

    @RequestMapping(method = [RequestMethod.POST], path = [REGISTER_URI + CONFIRM_URI])
    fun confirmRegistration(@RequestHeader(name = NoStrings.AUTH_HEADER) token: String?): User {
        return userService.confirmRegistration(token)
    }

    @RequestMapping(method = [RequestMethod.PUT], path = ["/{userId}"])
    fun updateUser(@PathVariable userId: String, @RequestBody user: User): User {
        return userService.updateUser(userId, user)
    }

    @RequestMapping(method = [RequestMethod.PUT], path = ["/{userId}$SECURE_URI"])
    fun secureUpdateUser(@PathVariable userId: String, @RequestBody update: SecureUserUpdate): User {
        return userService.secureUpdate(userId, update)
    }

    @RequestMapping(method = [RequestMethod.GET], path = [PROFILE_URI])
    fun profile(@RequestParam memberships: Boolean = false, @RequestParam rights: Boolean = false): User {
        return userService.getCurrentUser(memberships, rights)
    }

    @RequestMapping(method = [RequestMethod.DELETE], path = ["/{userId}"])
    fun deleteUser(@PathVariable userId: String, @RequestParam hard: Boolean = false): User {
        return userService.deleteUser(userId, hard)
    }

    @RequestMapping(method = [RequestMethod.GET], path = ["/{userId}"])
    fun getUser(@PathVariable userId: String): User {
        return userService.getUser(userId)
    }

    @RequestMapping(method = [RequestMethod.GET])
    fun getUsers(@RequestParam(defaultValue = "1") page: Int, @RequestParam(defaultValue = "20") size: Int,
                 @RequestParam id: Set<String>?,
                 @RequestParam email: Set<String>?,
                 @RequestParam username: Set<String>?,
                 @RequestParam group: Set<String>?,
                 @RequestParam status: Set<EntityStatus>?): NoPageResponse<User> {
        val pageable = NoPageable<User>(page, size, *UserSearchCriteria.DEFAULT_SORT_FIELDS)
        val result = userService.getUsers(
                UserSearchCriteria(userIds = id, memberGroupIds = group, usernames = username, emails = email, status = status, page = pageable))
        return pageable.toResponse(result)
    }

}
