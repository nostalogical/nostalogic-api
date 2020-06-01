package net.nostalogic.access.controllers

import net.nostalogic.access.services.SessionService
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.security.models.SessionSummary
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sessions")
class SessionController(private val sessionService: SessionService) {

    @RequestMapping(method = [RequestMethod.POST], produces = ["application/json"])
    fun createSession(@RequestBody prompt: SessionPrompt): SessionSummary {
        return sessionService.createSession(prompt)
    }

    @RequestMapping(method = [RequestMethod.GET], produces = ["application/json"])
    fun verifySession(@RequestHeader("Authorization") token: String): SessionSummary {
        return sessionService.verifySession(token)
    }

    @RequestMapping(method = [RequestMethod.PUT], produces = ["application/json"])
    fun refreshSession(@RequestHeader("Authorization") token: String): SessionSummary {
        return sessionService.refreshSession(token)
    }

    @RequestMapping(path= ["/update/{userId}"], method = [RequestMethod.PUT], produces = ["application/json"])
    fun updateUserSessions(@RequestBody groups: Set<String>, @PathVariable userId: String) {
        sessionService.updateUserSessions(userId, groups)
    }

    @RequestMapping(method = [RequestMethod.DELETE], produces = ["application/json"])
    fun expireSession(@RequestHeader("Authorization") token: String): SessionSummary {
        return sessionService.expireSession(token)
    }

}
