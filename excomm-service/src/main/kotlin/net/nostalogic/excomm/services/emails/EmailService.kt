package net.nostalogic.excomm.services.emails

import net.nostalogic.constants.MessageType
import net.nostalogic.constants.NoLocale
import net.nostalogic.datamodel.excomm.MessageOutline
import net.nostalogic.exceptions.NoRetrieveException
import net.nostalogic.exceptions.NoSaveException
import net.nostalogic.excomm.persistence.entities.EmailEntity
import net.nostalogic.excomm.persistence.entities.EmailTemplateEntity
import net.nostalogic.excomm.persistence.repositories.EmailRepository
import net.nostalogic.excomm.persistence.repositories.EmailTemplateRepository
import net.nostalogic.excomm.utils.ParameterUtils
import net.nostalogic.excomm.validators.EmailValidator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
abstract class EmailService {

    protected val logger: Logger = LoggerFactory.getLogger(EmailEntity::class.java)

    @Autowired private lateinit var emailRepository: EmailRepository
    @Autowired private lateinit var emailTemplateRepository: EmailTemplateRepository

    abstract fun sendEmail(messageOutline: MessageOutline): EmailEntity

    protected fun createEmailEntity(messageOutline: MessageOutline): EmailEntity {
        try {
            EmailValidator.validate(messageOutline)
            val template = getEmailTemplate(messageOutline.type, messageOutline.locale)
            val entity = EmailEntity(
                recipientId = messageOutline.recipientId,
                recipientEmailAddress = messageOutline.recipientEmailAddress,
                fromEmailAddress = template.fromEmailAddress,
                subject = ParameterUtils.setParameters(template.subject, messageOutline.parameters),
                bodyHtml = ParameterUtils.setParameters(template.bodyHtml, messageOutline.parameters),
                bodyPlain = ParameterUtils.setParameters(template.bodyPlain, messageOutline.parameters),
                type = messageOutline.type
            )
            return saveEmail(entity)
        } catch (e: Exception) {
            if (e !is NoSaveException)
                logger.error("Failed to create email", e)
            throw e
        }
    }

    protected fun saveEmail(emailEntity: EmailEntity): EmailEntity {
        return try {
            emailRepository.save(emailEntity)
        } catch (e: Exception) {
            logger.error("Failed to save email to ${emailEntity.recipientEmailAddress}", e)
            throw NoSaveException(405001, "email", e)
        }
    }

    private fun getEmailTemplate(type: MessageType, locale: NoLocale): EmailTemplateEntity {
        return emailTemplateRepository.findByTypeEqualsAndLocaleEquals(type, locale)
                ?: throw NoRetrieveException(404001, "EmailTemplate", "No template found for message type $type amd locale $locale")
    }

}
