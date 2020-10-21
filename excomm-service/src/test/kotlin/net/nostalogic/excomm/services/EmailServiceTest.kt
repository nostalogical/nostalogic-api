package net.nostalogic.excomm.services

import io.mockk.every
import io.mockk.verify
import net.nostalogic.constants.MessageType
import net.nostalogic.constants.NoLocale
import net.nostalogic.datamodel.excomm.MessageOutline
import net.nostalogic.exceptions.NoValidationException
import net.nostalogic.excomm.config.ExcommTestConfig
import net.nostalogic.excomm.persistence.entities.EmailEntity
import net.nostalogic.excomm.persistence.entities.EmailTemplateEntity
import net.nostalogic.excomm.persistence.repositories.EmailRepository
import net.nostalogic.excomm.persistence.repositories.EmailTemplateRepository
import net.nostalogic.excomm.services.emails.EmailService
import net.nostalogic.utils.EntityUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles(profiles = ["test"])
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [ExcommTestConfig::class])
class EmailServiceTest(@Autowired val emailService: EmailService,
                       @Autowired val emailRepository: EmailRepository,
                       @Autowired val emailTemplateRepository: EmailTemplateRepository) {

    private val TEST_SUBJECT = "Test Subject"
    private val TEST_BODY = "Test Body"
    private val SENDER = "noreply@nostalogictest.net"
    private val RECEIVER = "receiver@nostalogictest.net"

    @BeforeEach
    fun setup() {
    }

    fun mockTemplate(emailTemplateEntity: EmailTemplateEntity) {
        every { emailTemplateRepository.findByTypeEqualsAndLocaleEquals(MessageType.REGISTRATION_CONFIRM, NoLocale.en_GB) }
                .answers{ emailTemplateEntity }
    }

    fun mockSave() {
        every { emailRepository.save(ofType(EmailEntity::class)) }.answers{ firstArg() }
    }

    fun templateEntity(subject: String, body: String): EmailTemplateEntity {
        return EmailTemplateEntity(
                subject = subject,
                bodyHtml = body,
                bodyPlain = body,
                type = MessageType.REGISTRATION_CONFIRM,
                locale = NoLocale.en_GB,
                fromEmailAddress = SENDER)
    }

    fun outline(): MessageOutline {
        return MessageOutline(
                recipientId = EntityUtils.uuid(),
                recipientEmailAddress = RECEIVER,
                type = MessageType.REGISTRATION_CONFIRM,
                locale = NoLocale.en_GB)
    }

    @Test
    fun `Sending an email saves it in the database`() {
        mockTemplate(templateEntity(TEST_SUBJECT, TEST_BODY))
        mockSave()
        emailService.sendEmail(outline())
        verify(exactly = 1) { emailRepository.save(ofType(EmailEntity::class)) }
    }

    @Test
    fun `Create a test email`() {
        mockTemplate(templateEntity(TEST_SUBJECT, TEST_BODY))
        mockSave()
        val email = emailService.sendEmail(outline())
        Assertions.assertNotNull(email.id)
        Assertions.assertNotNull(email.recipientId)
        Assertions.assertEquals(TEST_SUBJECT, email.subject)
        Assertions.assertEquals(TEST_BODY, email.bodyHtml)
        Assertions.assertEquals(TEST_BODY, email.bodyPlain)
        Assertions.assertEquals(MessageType.REGISTRATION_CONFIRM, email.type)
        Assertions.assertEquals(SENDER, email.fromEmailAddress)
        Assertions.assertEquals(RECEIVER, email.recipientEmailAddress)
    }

    @Test
    fun `Confirm email replaces parameters`() {
        val subject = "Hello {{user}}! We have deals of up to {{offer}} off for you!"
        val body = "The deal was {{deal_type}}. This has been {{about}}. Better luck next time, {{userbye}}!"
        mockTemplate(templateEntity(subject, body))
        val outline = outline()
                .setParameter("user", "Jonnie boy")
                .setParameter("offer", "75%")
                .setParameter("deal_type", "a lie")
                .setParameter("about", "a deception for test purposes")
                .setParameter("userBye", "loser")
        mockSave()
        val email = emailService.sendEmail(outline)
        Assertions.assertEquals("Hello Jonnie boy! We have deals of up to 75% off for you!", email.subject)
        Assertions.assertEquals("The deal was a lie. This has been a deception for test purposes. Better luck next time, loser!", email.bodyPlain)
        Assertions.assertEquals(email.bodyPlain, email.bodyHtml)
    }

    @Test
    fun `Empty message outline throws an error`() {
        try {
            emailService.sendEmail(MessageOutline(recipientId = "", recipientEmailAddress = "", type = MessageType.REGISTRATION_CONFIRM, locale = NoLocale.en_GB))
        } catch (e: NoValidationException) {
            Assertions.assertEquals("fieldsInvalid", e.debugMessage)
        }
    }
}
