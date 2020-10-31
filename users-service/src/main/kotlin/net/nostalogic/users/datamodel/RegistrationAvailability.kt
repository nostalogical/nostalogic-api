package net.nostalogic.users.datamodel

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RegistrationAvailability(val usernameAvailable: Boolean?, val emailAvailable: Boolean?)
