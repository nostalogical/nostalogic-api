package net.nostalogic.comms

import khttp.responses.Response
import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.utils.Serialiser
import org.slf4j.LoggerFactory
import java.lang.reflect.Type

object Comms {

    private val logger = LoggerFactory.getLogger(Comms::class.java)
    val HEADERS = hashMapOf(Pair("Content-Type", "application/json"))
    var accessComms: AccessComms = AccessComms
    var excommComms: ExcommComms = ExcommComms

    fun access(): AccessComms { return accessComms }
    fun excomm(): ExcommComms { return excommComms }

    fun <T> parseResponse(response: Response, clazz: Class<T>? = null, type: Type? = null, expectedCode: Int = 200): T? {
        return try {
            if (response.statusCode != expectedCode) {
                logger.error("Internal request returned a non-OK response (${response.statusCode}): ${String(response.content)}")
                return null
            }
            if (type != null) Serialiser.fromJson(response.jsonObject, type = type) else Serialiser.fromJson(response.jsonObject, clazz = clazz)
        } catch (e: Exception) {
            logger.error("Internal request failed with an exception", e)
            null
        }
    }

    fun <T> parsePagedResponse(response: Response, type: Type, expectedCode: Int = 200): List<T>? {
        return try {
            if (response.statusCode != expectedCode) {
                logger.error("Internal request returned a non-OK response (${response.statusCode}): ${String(response.content)}")
                return null
            }
            val parsed = Serialiser.fromJson<NoPageResponse<T>>(response.jsonObject, type = type)
            return parsed?.content

        } catch (e: Exception) {
            logger.error("Internal request failed with an exception", e)
            null
        }
    }

}
