package net.nostalogic.users.datamodel.authentication

data class PasswordResetRequest(
        val username: String?,
        val password: String?,
        val newPassword: String?)
