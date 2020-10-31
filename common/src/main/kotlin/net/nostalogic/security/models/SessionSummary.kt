package net.nostalogic.security.models

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.NoDate

class SessionSummary(
        var sessionId: String,
        var userId: String,
        var type: AuthenticationType,
        var start: NoDate,
        var end: NoDate,
        var description: String?,
        var token: String?
)
