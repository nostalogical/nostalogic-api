package net.nostalogic.users.controllers

import net.nostalogic.constants.NoStrings
import net.nostalogic.security.contexts.SessionContext
import net.nostalogic.users.UsersApplication
import net.nostalogic.users.controllers.UserController.Companion.USERS_ENDPOINT
import net.nostalogic.users.datamodel.RegistrationAvailability
import net.nostalogic.users.datamodel.User
import net.nostalogic.users.datamodel.UserRegistration
import net.nostalogic.users.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(USERS_ENDPOINT, produces = ["application/json"], consumes = ["application/json"])
class UserController(@Autowired val userService: UserService) {

    companion object {
        const val USERS_ENDPOINT = "/v${UsersApplication.MAJOR}/users"
        const val REGISTER_URI = "/register"
        const val AVAILABLE_URI = "/check"
        const val CONFIRM_URI = "/confirm"
        const val SECURE_URI = "/secure"
    }

    @RequestMapping(method = [RequestMethod.POST])
    fun createUser(@RequestBody userRegistration: UserRegistration): User {
        SessionContext.requireLogin()
        return userService.createUser(userRegistration)
    }

    @RequestMapping(method = [RequestMethod.POST], path = [REGISTER_URI])
    fun register(@RequestBody userRegistration: UserRegistration): User {
        return userService.registerUser(userRegistration)
    }

    @RequestMapping(method = [RequestMethod.POST], path = [REGISTER_URI + AVAILABLE_URI])
    fun availableCheck(@RequestBody userRegistration: UserRegistration): RegistrationAvailability {
        return userService.checkRegistrationAvailable(userRegistration)
    }

    @RequestMapping(method = [RequestMethod.POST], path = [REGISTER_URI + CONFIRM_URI])
    fun confirmRegistration(@RequestHeader(name = NoStrings.AUTH_HEADER) token: String?): User {
        return userService.confirmRegistration(token)
    }

    @RequestMapping(method = [RequestMethod.PUT], path = ["/{userId}"])
    fun updateUser(@PathVariable userId: String, @RequestBody user: User) {
        // TODO
    }

    @RequestMapping(method = [RequestMethod.PUT], path = ["/{userId}$SECURE_URI"])
    fun secureUpdateUser(@PathVariable userId: String, @RequestBody user: User) {
        // TODO
    }

    @RequestMapping(method = [RequestMethod.DELETE])
    fun deleteUser() {
        // TODO
    }

    @RequestMapping(method = [RequestMethod.GET], path = ["/{userId}"])
    fun getUser(@PathVariable userId: String) {
        // TODO
    }

    @RequestMapping(method = [RequestMethod.GET])
    fun getUsers() {
        // TODO
    }

}
