package net.nostalogic.excomm.controllers

import net.nostalogic.datamodel.excomm.MessageOutline
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity
import net.nostalogic.excomm.ExcommApplication
import net.nostalogic.excomm.controllers.ExcommController.Companion.EXCOMM_ENDPOINT
import net.nostalogic.excomm.services.emails.EmailService
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(EXCOMM_ENDPOINT, produces = ["application/json"])
class ExcommController(val emailService: EmailService) {

    companion object {
        const val EXCOMM_ENDPOINT = "/v${ExcommApplication.MAJOR}/excomm"
        const val MESSAGE_URI = "/message"
    }

    @RequestMapping(method = [RequestMethod.POST], produces = ["application/json"], path = [MESSAGE_URI])
    fun sendMessage(@RequestBody messageOutline: MessageOutline): String {
        val email = emailService.sendEmail(messageOutline)
        return EntityReference(email.id, NoEntity.EMAIL).toString()
    }

}
