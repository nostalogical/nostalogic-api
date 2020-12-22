package net.nostalogic.datamodel.access

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class EntityRights(
    val read: Boolean? = null,
    val create: Boolean? = null,
    val edit: Boolean? = null,
    val delete: Boolean? = null
)
