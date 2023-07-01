package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.NoDate

class ImpersonationGrant(
        subject: String,
        expiration: NoDate,
        val sessionId: String,
        val originalSubject: String,
        created: NoDate = NoDate()
) : NoGrant(
        subject,
        expiration,
        AuthenticationType.IMPERSONATION,
        created = created
)
