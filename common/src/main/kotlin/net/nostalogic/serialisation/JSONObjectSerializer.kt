package net.nostalogic.serialisation

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.json.JSONObject

class JSONObjectSerializer : JsonSerializer<JSONObject>() {
    override fun serialize(
        jsonObject: JSONObject?,
        jsonGenerator: JsonGenerator?,
        serializerProvider: SerializerProvider?
    ) {
        if (jsonObject != null) {
            jsonGenerator?.writeRawValue(jsonObject.toString())
        }
    }
}
