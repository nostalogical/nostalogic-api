package net.nostalogic.users.datamodel.users

data class SecureUserUpdate(
        var currentPassword: String? = null,
        var email: String? = null,
        var password: String? = null
)
