package net.nostalogic.excomm.persistence.entities

import net.nostalogic.constants.MessageType
import net.nostalogic.constants.NoLocale
import net.nostalogic.persistence.entities.AbstractCoreEntity
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity(name = "email_template")
class EmailTemplateEntity(
        val subject: String,
        val bodyHtml: String,
        val bodyPlain: String,
        @Enumerated(EnumType.STRING) val type: MessageType,
        @Enumerated(EnumType.STRING) val locale: NoLocale,
        val fromEmailAddress: String
): AbstractCoreEntity()
