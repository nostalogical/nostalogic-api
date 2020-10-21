package net.nostalogic.excomm.persistence.repositories

import net.nostalogic.constants.MessageType
import net.nostalogic.constants.NoLocale
import net.nostalogic.excomm.persistence.entities.EmailTemplateEntity
import org.springframework.data.jpa.repository.JpaRepository

interface EmailTemplateRepository: JpaRepository<EmailTemplateEntity, String> {

    fun findByTypeEqualsAndLocaleEquals(type: MessageType, locale: NoLocale): EmailTemplateEntity?
}
