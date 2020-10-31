package net.nostalogic.datamodel

import com.fasterxml.jackson.annotation.JsonInclude
import net.nostalogic.entities.NoEntity

@JsonInclude(JsonInclude.Include.NON_NULL)
class ChangeSummary(
        val changed: Boolean,
        val id: String,
        val entity: NoEntity,
        val failureReason: String? = null)
