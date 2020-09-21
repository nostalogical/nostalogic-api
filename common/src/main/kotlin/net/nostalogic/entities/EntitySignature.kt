package net.nostalogic.entities

import net.nostalogic.utils.EntityUtils

data class EntitySignature(val id: String, val entity: NoEntity) {

    fun toLocalReference(): String {
        return EntityUtils.localReference(id, entity)
    }

    fun toEntityReference(): EntityReference {
        return EntityUtils.entityReference(id, entity)
    }
}
