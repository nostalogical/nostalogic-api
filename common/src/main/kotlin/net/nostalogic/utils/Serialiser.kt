package net.nostalogic.utils

import com.google.gson.GsonBuilder
import net.nostalogic.datamodel.NoDate
import org.json.JSONObject
import org.slf4j.LoggerFactory

object Serialiser {

    private val logger = LoggerFactory.getLogger(Serialiser::class.java)
    private val gson = GsonBuilder().registerTypeAdapter(NoDate::class.java, NoDate.NoDateDeserializer()).create()

    fun serialise(src: Any): String {
        return gson.toJson(src)
    }

    fun <T> deserialise(src: String, clazz: Class<T>?): T? {
        return try {
            gson.fromJson(src, clazz)
        } catch (e: Exception) {
            logger.error("Failed to deserialise object", e)
            null
        }
    }

    fun toJson(src: Any): JSONObject? {
        return try {
            JSONObject(serialise(src))
        } catch (e: Exception) {
            logger.error("Failed to convert object to JSONObject", e)
            null
        }
    }

    fun <T> fromJson(src: JSONObject, clazz: Class<T>?): T? {
        return deserialise(src.toString(), clazz)
    }
}
