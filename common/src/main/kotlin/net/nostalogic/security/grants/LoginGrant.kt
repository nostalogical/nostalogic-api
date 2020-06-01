package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.NoDate

class LoginGrant(
        subject: String,
        val additional: Set<String>,
        expiration: NoDate,
        val sessionId: String,
        type: AuthenticationType,
        created: NoDate = NoDate()
) : NoGrant(
        subject,
        expiration,
        type,
        created = created)
