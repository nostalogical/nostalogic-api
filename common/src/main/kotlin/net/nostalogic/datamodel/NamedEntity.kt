package net.nostalogic.datamodel

import net.nostalogic.entities.NoEntity

data class NamedEntity(
        val name: String?,
        val id: String?,
        val entity: NoEntity
)
