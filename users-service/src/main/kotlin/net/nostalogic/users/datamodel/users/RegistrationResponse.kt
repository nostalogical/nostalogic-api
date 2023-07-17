package net.nostalogic.users.datamodel.users

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RegistrationResponse(
    val email: String,
    val displayName: String,
)
