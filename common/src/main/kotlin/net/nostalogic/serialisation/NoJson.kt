package net.nostalogic.serialisation

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.json.JSONObject

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonSerialize(using = JSONObjectSerializer::class)
class NoJson(src: String?) : JSONObject(src) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        return toString() == other.toString()
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
