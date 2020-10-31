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
            val response = khttp.post(url = Config.excommUrl() + EXCOMM_MESSAGE_ENDPOINT, json = Serialiser.toJson(messageOutline), headers = Comms.HEADERS)
            if (response.statusCode != 200) {
                logger.error("Excomm request returned non-OK response (${response.statusCode}): ${String(response.content)}")
                null
            } else {
                val messageId = String(response.content)
                logger.info("Sent ${messageOutline.type} to ${messageOutline.recipientId} as $messageId")
                messageId
            }
        } catch (e: Exception) {
            logger.error("Failed to send ${messageOutline.type} to ${messageOutline.recipientId}", e)
            null
        }
    }

}
