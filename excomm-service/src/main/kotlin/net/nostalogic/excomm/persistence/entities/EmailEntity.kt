package net.nostalogic.excomm.persistence.entities

import net.nostalogic.constants.MessageType
import net.nostalogic.excomm.constants.EmailStatus
import net.nostalogic.persistence.entities.AbstractCoreEntity
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity(name = "email")
class EmailEntity(
        val recipientId: String?,
        val recipientEmailAddress: String,
        val fromEmailAddress: String,
        val subject: String,
        val bodyHtml: String,
        val bodyPlain: String,
        @Enumerated(EnumType.STRING) val type: MessageType,
        @Enumerated(EnumType.STRING) var status: EmailStatus = EmailStatus.SENT,
        var failReason: String? = null
    ): AbstractCoreEntity()
