package net.nostalogic.entities

import net.nostalogic.utils.EntityUtils

data class EntityReference(val id: String?, val entity: NoEntity) {

    fun toLocalReference(): String {
        return EntityUtils.localReference(id, entity)
    }

    fun toEntityReference(): String {
        return EntityUtils.entityReference(id, entity)
    }

    fun isSignature(): Boolean {
        return id != null
    }
}
