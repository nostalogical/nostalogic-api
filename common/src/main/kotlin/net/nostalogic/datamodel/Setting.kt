package net.nostalogic.datamodel

import com.fasterxml.jackson.annotation.JsonIgnore
import org.apache.commons.lang3.math.NumberUtils

data class Setting(@JsonIgnore val key: String, @JsonIgnore val initValue: Any, val source: Source) {

    val value = initValue.toString()
    val numValue: Int? = if (NumberUtils.isParsable(value)) NumberUtils.toInt(value) else null

    enum class Source {
        RESOURCE,
        DATABASE,
        SERVICE
    }

}
