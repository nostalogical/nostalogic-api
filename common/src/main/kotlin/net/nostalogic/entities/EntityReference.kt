package net.nostalogic.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import net.nostalogic.utils.EntityUtils

class EntityReference {

    val id: String?
    val entity: NoEntity

    constructor(entityId: String) {
        val split = entityId.split(EntityUtils.DELIMITER)
        this.id = if (split.size == 2) split[1] else null
        this.entity = EntityUtils.toEntity(split[0])
    }

    constructor(id: String? = null, entity: NoEntity) {
        this.id = id
        this.entity = entity
    }

    fun toLocalReference(): String {
        return EntityUtils.localReference(id, entity)
    }

    @JsonValue
    override fun toString(): String {
        return entity.name + (if (isSignature()) EntityUtils.DELIMITER + id else "")
    }

    @JsonIgnore
    fun isSignature(): Boolean {
        return id != null
    }

    override fun equals(other: Any?): Boolean {
        if (other is String)
            return other == toString()
        else if (other is EntityReference)
            return other.toString().equals(toString(), true)
        else if (other is NoEntity)
            return other.name.equals(toString(), true)
        return false
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + entity.hashCode()
        return result
    }
}
