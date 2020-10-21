package net.nostalogic.excomm.services.emails

import net.nostalogic.datamodel.excomm.MessageOutline
import net.nostalogic.excomm.persistence.entities.EmailEntity

class LocalEmailService: EmailService() {

    override fun sendEmail(messageOutline: MessageOutline): EmailEntity {
        return createEmailEntity(messageOutline)
    }
}
