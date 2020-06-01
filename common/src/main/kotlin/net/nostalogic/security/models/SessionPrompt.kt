package net.nostalogic.security.models

import net.nostalogic.constants.AuthenticationType
import java.io.Serializable

class SessionPrompt(
        val userId: String,
        val additional: Set<String>,
        val type: AuthenticationType) : Serializable
