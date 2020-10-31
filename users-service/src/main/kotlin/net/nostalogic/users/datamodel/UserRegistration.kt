package net.nostalogic.users.datamodel

import net.nostalogic.constants.NoLocale
import net.nostalogic.security.contexts.SessionContext

data class UserRegistration(
        val username: String?,
        val email: String?,
        val password: String?,
        val resetPassword: Boolean?,
        val locale: NoLocale = SessionContext.getLocale()
)
