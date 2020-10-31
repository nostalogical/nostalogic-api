package net.nostalogic.users.datamodel.authentication

data class LoginRequest(
        val username: String?,
        val password: String?,
        val newPassword: String?)
