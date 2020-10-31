package net.nostalogic.comms

import khttp.responses.Response
import net.nostalogic.utils.Serialiser
import org.slf4j.LoggerFactory

object Comms {

    private val logger = LoggerFactory.getLogger(Comms::class.java)
    val HEADERS = hashMapOf(Pair("Content-Type", "application/json"))
    var accessComms: AccessComms = AccessComms
    var excommComms: ExcommComms = ExcommComms

    fun access(): AccessComms { return accessComms }
    fun excomm(): ExcommComms { return excommComms }

    fun <T> exchange(response: Response, clazz: Class<T>, expectedCode: Int = 200): T? {
        return try {
            if (response.statusCode != expectedCode) {
                logger.error("Internal request a returned non-OK response (${response.statusCode}): ${String(response.content)}")
                return null
            }
            Serialiser.fromJson(response.jsonObject, clazz)
        } catch (e: Exception) {
            logger.error("Internal request failed with an exception", e)
            null
        }
    }

}
