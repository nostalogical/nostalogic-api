package net.nostalogic.security.models

import net.nostalogic.constants.AuthenticationSource
import java.io.Serializable

class SessionPrompt(
        val userId: String,
        val type: AuthenticationSource,
        val originalUserId: String? = null,
        val reset: Boolean = false,
        val tenant: String?,
) : Serializable
