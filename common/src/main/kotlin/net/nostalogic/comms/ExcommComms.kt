package net.nostalogic.comms

import net.nostalogic.config.Config
import net.nostalogic.datamodel.excomm.MessageOutline
import net.nostalogic.utils.Serialiser
import org.slf4j.LoggerFactory

object ExcommComms {

    private val logger = LoggerFactory.getLogger(ExcommComms::class.java)
    private const val EXCOMM_MESSAGE_ENDPOINT = "/v0/excomm/message"

    fun send(messageOutline: MessageOutline): String? {
        val response = khttp.post(url = Config.excommUrl() + EXCOMM_MESSAGE_ENDPOINT, json = Serialiser.toJson(messageOutline), headers = BaseComms.HEADERS)
        if (response.statusCode != 200) {
            logger.error("Excomm request returned non-OK response (${response.statusCode}): ${String(response.content)}")
            return null
        }
        return String(response.content)
    }

}
