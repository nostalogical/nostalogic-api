package net.nostalogic.comms

import net.nostalogic.config.Config
import net.nostalogic.datamodel.excomm.MessageOutline
import net.nostalogic.utils.Serialiser
import org.slf4j.LoggerFactory

object ExcommComms {

    private val logger = LoggerFactory.getLogger(ExcommComms::class.java)
    private const val EXCOMM_MESSAGE_ENDPOINT = "/v0/excomm/message"

    fun send(messageOutline: MessageOutline): String? {
        logger.info("Sending ${messageOutline.type} to ${messageOutline.recipientId}")
        return try {
            val messageId = Comms.autoPost(url = Config.excommUrl() + EXCOMM_MESSAGE_ENDPOINT, json = Serialiser.serialise(messageOutline), headers = Comms.HEADERS, expectedCode = 200, clazz = String::class.java)
            logger.info("Sent ${messageOutline.type} to ${messageOutline.recipientId} as $messageId")
            messageId
        } catch (e: Exception) {
            logger.error("Failed to send ${messageOutline.type} to ${messageOutline.recipientId}", e)
            null
        }
    }

}
