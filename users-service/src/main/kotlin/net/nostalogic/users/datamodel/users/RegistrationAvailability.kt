package net.nostalogic.users.datamodel.users

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RegistrationAvailability(
    val usernameAvailable: Boolean?,
    val taggingEnabled: Boolean,
    val emailAvailable: Boolean? = null,
)
