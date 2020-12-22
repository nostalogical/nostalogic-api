package net.nostalogic.users.controllers

import net.nostalogic.security.contexts.SessionContext
import net.nostalogic.users.UsersApplication
import net.nostalogic.users.controllers.AuthenticationController.Companion.AUTH_ENDPOINT
import net.nostalogic.users.datamodel.authentication.AuthenticationResponse
import net.nostalogic.users.datamodel.authentication.ImpersonationRequest
import net.nostalogic.users.datamodel.authentication.LoginRequest
import net.nostalogic.users.services.UserAuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping(AUTH_ENDPOINT, produces = ["application/json"])
class AuthenticationController(@Autowired private val userAuthService: UserAuthService) {

    companion object {
        const val AUTH_ENDPOINT = "/api/v${UsersApplication.MAJOR}/auth"
        const val LOGIN_URI = "/login"
        const val LOGOUT_URI = "/logout"
        const val REFRESH_URI = "/refresh"
        const val PASSWORD_URI = "/resetPassword"
        const val IMPERSONATE_URI = "/impersonate"
    }

    @RequestMapping(method = [RequestMethod.POST], produces = ["application/json"], path = [LOGIN_URI])
    fun login(@RequestBody loginRequest: LoginRequest): AuthenticationResponse {
        return userAuthService.login(loginRequest)
    }

    @RequestMapping(method = [RequestMethod.POST], produces = ["application/json"], path = [LOGOUT_URI])
    fun logout(): AuthenticationResponse {
        SessionContext.requireLogin()
        return userAuthService.logout()
    }

    @RequestMapping(method = [RequestMethod.POST], produces = ["application/json"], path = [REFRESH_URI])
    fun refresh(): AuthenticationResponse {
        SessionContext.requireLogin()
        return userAuthService.refresh()
    }

    @RequestMapping(method = [RequestMethod.POST], produces = ["application/json"], path = [PASSWORD_URI])
    fun resetPassword(@RequestBody loginRequest: LoginRequest): AuthenticationResponse {
        return userAuthService.resetPassword(loginRequest)
    }

    @RequestMapping(method = [RequestMethod.POST], produces = ["application/json"], path = [IMPERSONATE_URI])
    fun impersonate(@RequestBody impersonationRequest: ImpersonationRequest): AuthenticationResponse {
        SessionContext.requireLogin()
        return userAuthService.impersonate(impersonationRequest)
    }

}
