package net.nostalogic.access.controllers

import net.nostalogic.access.AccessApplication
import net.nostalogic.access.controllers.SessionController.Companion.SESSIONS_ENDPOINT
import net.nostalogic.access.services.SessionService
import net.nostalogic.constants.NoStrings
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.security.models.SessionSummary
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(SESSIONS_ENDPOINT)
class SessionController(private val sessionService: SessionService) {

    companion object {
        const val SESSIONS_ENDPOINT = "/api/v${AccessApplication.MAJOR}/sessions"
    }

    @RequestMapping(method = [RequestMethod.POST], produces = ["application/json"])
    fun createSession(@RequestBody prompt: SessionPrompt): SessionSummary {
        return sessionService.createSession(prompt)
    }

    @RequestMapping(method = [RequestMethod.GET], produces = ["application/json"])
    fun verifySession(@RequestHeader(NoStrings.AUTH_HEADER) token: String): SessionSummary {
        return sessionService.verifySession(token)
    }

    @RequestMapping(method = [RequestMethod.PUT], produces = ["application/json"])
    fun refreshSession(@RequestHeader(NoStrings.AUTH_HEADER) token: String): SessionSummary {
        return sessionService.refreshSession(token)
    }

    @RequestMapping(method = [RequestMethod.DELETE], produces = ["application/json"])
    fun expireSession(@RequestHeader(NoStrings.AUTH_HEADER) token: String): SessionSummary {
        return sessionService.expireSession(token)
    }

    @RequestMapping(path= ["/update/{userId}"], method = [RequestMethod.PUT], produces = ["application/json"])
    fun updateUserSessions(@RequestBody groups: HashSet<String>, @PathVariable userId: String) {
        sessionService.updateUserSessions(userId, groups)
    }

}
