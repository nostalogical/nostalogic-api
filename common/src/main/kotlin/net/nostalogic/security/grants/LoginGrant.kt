package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.NoDate

/**
 * A login grant is a short-lived grant that permits direct access to a user's account.
 */
class LoginGrant(
        subject: String,
        expiration: NoDate,
        val sessionId: String,
        created: NoDate = NoDate()
) : NoGrant(
        subject,
        expiration,
        AuthenticationType.LOGIN,
        created = created)
