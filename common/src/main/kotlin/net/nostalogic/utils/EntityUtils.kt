package net.nostalogic.utils

import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoValidationException
import java.util.*

object EntityUtils {

    private const val DELIMITER = "_"
    const val SYSTEM_ID = "SYSTEM_GENERATED_RECORD_____________"

    fun uuid(): String {
        return UUID.randomUUID().toString()
    }

    fun shortEntityId(id: String?, entity: NoEntity): String {
        if (id != null)
            return id
        return entity.name
    }

    fun fullEntityId(id: String?, entity: NoEntity): String {
        if (id == null)
            return entity.name
        return entity.name.toLowerCase() + DELIMITER + id
    }

    fun isShortId(entityId: String): Boolean {
        if (isEntity(entityId))
            return true
        return isUuid(entityId)
    }

    fun isFullId(entityId: String): Boolean {
        try {
            toEntityRef(entityId)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun toMaybeEntityRef(entityId: String): EntityReference? {
        return try {
            toEntityRef(entityId)
        } catch (e: Exception) {
            null
        }
    }

    fun isEntity(entity: String): Boolean {
        for (ent in NoEntity.values())
            if (ent.name.equals(entity, true))
                return true
        return false
    }

    fun isUuid(id: String): Boolean {
        try {
            UUID.fromString(id)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun toEntityRef(entityId: String): EntityReference {
        val split = entityId.split(DELIMITER)
        val ref: EntityReference
        ref = if (split.size == 2)
            EntityReference(split[1], toEntity(split[0]))
        else EntityReference(null, toEntity(entityId))
        return ref
    }

    fun toEntity(string: String): NoEntity {
        for (entity in NoEntity.values()) {
            if (entity.name.equals(string, true))
                return entity
        }
        throw NoValidationException(107004, "entity", "'${string}' is not a valid entity")
    }

}
