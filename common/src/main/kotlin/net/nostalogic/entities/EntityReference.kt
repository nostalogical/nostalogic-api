package net.nostalogic.entities

import net.nostalogic.utils.EntityUtils

data class EntityReference(val id: String?, val entity: NoEntity) {

    fun toShortId(): String {
        return EntityUtils.shortEntityId(id, entity)
    }

    fun toFullId(): String {
        return EntityUtils.fullEntityId(id, entity)
    }

    fun isSignature(): Boolean {
        return id != null
    }
}
