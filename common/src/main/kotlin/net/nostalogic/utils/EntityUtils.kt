package net.nostalogic.utils

import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoValidationException
import java.util.*

/**
 * UUID: A 36 character mixed case alphanumeric string, including 4 hyphens
 * Entity: The name of a persisted set of data, e.g. a user
 * Entity Signature: A UUID prefixed with the entity type, e.g. user_fb59f072-1adf-4097-8a85-c0502ce49b7e
 * Entity Reference: Either an entity signature or an entity
 * Local Reference: Either a UUID or an entity
 */
object EntityUtils {

    const val DELIMITER = "_"
    const val SYSTEM_ID = "SYSTEM_GENERATED_RECORD_____________"

    fun uuid(): String {
        return UUID.randomUUID().toString()
    }

    fun localReference(id: String?, entity: NoEntity): String {
        if (id != null)
            return id
        return entity.name
    }

    fun entityReference(id: String?, entity: NoEntity): EntityReference {
        if (id == null)
            return EntityReference(entity = entity)
        return EntityReference(id, entity)
    }

    fun isLocalReference(entityId: String): Boolean {
        if (isEntity(entityId))
            return true
        return isUuid(entityId)
    }

    fun isEntityReference(entityId: String): Boolean {
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
