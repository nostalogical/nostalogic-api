package net.nostalogic.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import net.nostalogic.utils.EntityUtils

data class EntityReference(val id: String? = null, val entity: NoEntity) {

    fun toLocalReference(): String {
        return EntityUtils.localReference(id, entity)
    }

    override fun toString(): String {
        return entity.name + (if (isSignature()) EntityUtils.DELIMITER + id else "")
    }

    @JsonIgnore
    fun isSignature(): Boolean {
        return id != null
    }
}
