package net.nostalogic.utils

import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import net.nostalogic.datamodel.NoDate
import net.nostalogic.serialisation.NoJson
import net.nostalogic.datamodel.access.PolicyAction
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.lang.reflect.Type
import java.util.*

object Serialiser {

    private val logger = LoggerFactory.getLogger(Serialiser::class.java)
    private val gson = GsonBuilder()
            .registerTypeAdapter(NoDate::class.java, NoDate.NoDateDeserializer())
            .registerTypeAdapter(EnumMap::class.java, EnumMapDeserializer()).create()

    fun serialise(src: Any): String {
        return gson.toJson(src)
    }

    fun <T> classDeserialise(src: String, clazz: Class<T>?): T? {
        return try {
            gson.fromJson(src, clazz)
        } catch (e: Exception) {
            logger.error("Failed to deserialise object", e)
            null
        }
    }

    fun <T> typeDeserialise(src: String, type: Type): T? {
        return try {
            gson.fromJson<T>(src, type)
        } catch (e: Exception) {
            logger.error("Failed to deserialise object", e)
            null
        }
    }

    fun toJsonObject(src: Any): NoJson? {
        if (src is NoJson) return src
        return try {
            val json = if (src is String) src else serialise(src)
            NoJson(json)
        } catch (e: Exception) {
            logger.error("Failed to convert object to JSONObject", e)
            null
        }
    }

    fun isValidJson(src: Any): Boolean {
        return toJsonObject(src) != null
    }

    fun <T> fromJson(src: JSONObject, clazz: Class<T>?): T? {
        return classDeserialise(src.toString(), clazz)
    }

    fun <T> fromJson(src: JSONObject, type: Type): T? {
        return typeDeserialise(src.toString(), type)
    }

    class EnumMapDeserializer: InstanceCreator<EnumMap<PolicyAction, Boolean>> {
        override fun createInstance(type: Type): EnumMap<PolicyAction, Boolean> {
            return EnumMap<PolicyAction, Boolean>(PolicyAction::class.java)
        }
    }
}
