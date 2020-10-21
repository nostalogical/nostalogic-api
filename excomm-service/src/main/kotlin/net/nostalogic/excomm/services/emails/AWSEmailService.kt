package net.nostalogic.excomm.services.emails

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemailv2.AmazonSimpleEmailServiceV2
import com.amazonaws.services.simpleemailv2.AmazonSimpleEmailServiceV2Client
import com.amazonaws.services.simpleemailv2.model.*
import net.nostalogic.datamodel.excomm.MessageOutline
import net.nostalogic.excomm.constants.EmailStatus
import net.nostalogic.excomm.persistence.entities.EmailEntity

class AWSEmailService(accessKey: String, secretKey: String): EmailService() {

    private val awsClient: AmazonSimpleEmailServiceV2 = AmazonSimpleEmailServiceV2Client.builder()
            .withRegion(Regions.EU_WEST_1)
            .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(
                    accessKey,
                    secretKey)))
            .build()
    private val emailCharset = "UTF-8"

    override fun sendEmail(messageOutline: MessageOutline): EmailEntity {
        val emailEntity = createEmailEntity(messageOutline)
        return try {
            val emailRequest = SendEmailRequest()
                    .withDestination(Destination().withToAddresses(emailEntity.recipientEmailAddress))
                    .withFromEmailAddress(emailEntity.fromEmailAddress)
                    .withContent(EmailContent().withSimple(Message()
                            .withSubject(Content()
                                    .withCharset(emailCharset)
                                    .withData(emailEntity.subject))
                            .withBody(Body()
                                    .withHtml(Content()
                                            .withCharset(emailCharset)
                                            .withData(emailEntity.bodyHtml))
                                    .withText(Content()
                                            .withCharset(emailCharset)
                                            .withData(emailEntity.bodyPlain)))))


//                    .withMessage(Message()
//                            .withSubject(Content()
//                                    .withCharset(emailCharset)
//                                    .withData(emailEntity.subject))
//                            .withBody(Body()
//                                    .withHtml(Content()
//                                            .withCharset(emailCharset)
//                                            .withData(emailEntity.bodyHtml))
//                                    .withText(Content()
//                                            .withCharset(emailCharset)
//                                            .withData(emailEntity.bodyPlain))))
//                    .withSource(emailEntity.fromEmailAddress)
            awsClient.sendEmail(emailRequest)
            return emailEntity
        } catch (e: Exception) {
            logger.error("Failed to send email ${emailEntity.id} via AWS", e)
            emailEntity.status = EmailStatus.FAILED
            emailEntity.failReason = e.message
            saveEmail(emailEntity)
        }
    }

}
