package net.nostalogic.comms

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import net.nostalogic.datamodel.ErrorResponse
import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.utils.Serialiser
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.lang.reflect.Type

object Comms {

    private val logger = LoggerFactory.getLogger(Comms::class.java)
    val HEADERS = hashMapOf(Pair("Content-Type", "application/json"))
    var accessComms: AccessComms = AccessComms
    var excommComms: ExcommComms = ExcommComms

    fun access(): AccessComms { return accessComms }
    fun excomm(): ExcommComms { return excommComms }

    private fun createParameters(params: Map<String, String>): List<Pair<String, String>> {
        return params.map { p -> Pair(p.key, p.value) }
    }

    fun <T> autoGetPaged(url: String, headers: Map<String, String>, params: Map<String, String> = emptyMap(), type: Type): List<T>? {
        val (_, response, result) = Fuel.get(url, createParameters(params))
            .header(headers).responseString()
        return parsePagedResponse(response, result, type)
    }

    fun <T> autoGet(url: String, headers: Map<String, String>, params: Map<String, String> = emptyMap(), clazz: Class<T>? = null, type: Type? = null): T? {
        val (_, response, result) = Fuel.get(url, createParameters(params))
            .header(headers).responseString()
        return parseResponse(response, result, clazz, type)
    }

    fun <T> autoPost(url: String, json: String, headers: Map<String, String>, clazz: Class<T>? = null, type: Type? = null, expectedCode: Int? = null): T? {
        val (_, response, result) = Fuel.post(url).jsonBody(json).header(headers)
            .responseString()
        return parseResponse(response, result, clazz, type, expectedCode)
    }

    fun <T> autoPut(url: String, json: String = "{}", headers: Map<String, String>, clazz: Class<T>? = null, type: Type? = null, expectedCode: Int? = null): T? {
        val (_, response, result) = Fuel.put(url).jsonBody(json).header(headers)
            .responseString()
        return parseResponse(response, result, clazz, type, expectedCode)
    }

    fun <T> autoDelete(url: String, json: String = "{}", headers: Map<String, String>, clazz: Class<T>? = null, type: Type? = null, expectedCode: Int? = null): T? {
        val (_, response, result) = Fuel.delete(url).jsonBody(json).header(headers)
            .responseString()
        return parseResponse(response, result, clazz, type, expectedCode)
    }

    private fun <T> parseResponse(response: com.github.kittinunf.fuel.core.Response, result: Result<String, FuelError>, clazz: Class<T>? = null, type: Type? = null, expectedCode: Int? = null): T? {
        if (clazz == null && type == null) {
            logger.error("No response type was provided for parsing of request response")
            return null
        }
        when (result) {
            is Result.Success -> {
                if (expectedCode != null && response.statusCode != expectedCode) {
                    logger.error("Internal request returned a non-OK response (${response.statusCode}): ${String(response.data)}")
                    return null
                }
                return if (type != null) Serialiser.typeDeserialise(result.value, type = type) else Serialiser.classDeserialise(result.value, clazz = clazz)
            }
            is Result.Failure -> {
                val bodyString = String(response.body().toByteArray())
                val noError = Serialiser.classDeserialise(bodyString, clazz = ErrorResponse::class.java)
                if (bodyString.contains("errorCode") && noError != null) {
                    MDC.put("errorCode", noError.errorCode.toString())
                    logger.error("Internal request failed (${noError.errorCode}): ${noError.debugMessage}")
                } else
                    logger.error("Internal request failed with an exception", result.getException())
                return null
            }
        }
    }

    private fun <T> parsePagedResponse(response: com.github.kittinunf.fuel.core.Response, result: Result<String, FuelError>, type: Type, expectedCode: Int = 200): List<T>? {        when (result) {
            is Result.Success -> {
                if (response.statusCode != expectedCode) {
                    logger.error("Internal request returned a non-OK response (${response.statusCode}): ${String(response.data)}")
                    return null
                }
                val parsed = Serialiser.typeDeserialise<NoPageResponse<T>>(result.value, type = type)
                return parsed?.content
            }
            is Result.Failure -> {
                logger.error("Internal request failed with an exception", result.getException())
                return null
            }
        }
    }

}
