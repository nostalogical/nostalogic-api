package net.nostalogic.security.models

import net.nostalogic.constants.AuthenticationType
import java.io.Serializable

class SessionPrompt(
        val userId: String,
        val additional: Set<String>,
        val type: AuthenticationType,
        val originalUserId: String? = null,
        val alternates: Set<String>? = null,
        val reset: Boolean = false) : Serializable
