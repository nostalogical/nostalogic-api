package net.nostalogic.excomm.persistence.entities

import net.nostalogic.constants.MessageType
import net.nostalogic.excomm.constants.EmailStatus
import net.nostalogic.persistence.entities.AbstractCoreEntity
import javax.persistence.Entity

@Entity(name = "email")
class EmailEntity(
        val recipientId: String?,
        val recipientEmailAddress: String,
        val fromEmailAddress: String,
        val subject: String,
        val bodyHtml: String,
        val bodyPlain: String,
        val type: MessageType,
        var status: EmailStatus = EmailStatus.SENT,
        var failReason: String? = null
    ): AbstractCoreEntity()
