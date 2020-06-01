package net.nostalogic.security.models

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.NoDate

class SessionSummary(
        val sessionId: String,
        val userId: String,
        val type: AuthenticationType,
        val start: NoDate,
        val end: NoDate,
        val description: String?,
        val token: String?
)
