package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.Tenant
import net.nostalogic.datamodel.NoDate

/**
 * A login grant is a short-lived grant that permits direct access to a user's account.
 */
class LoginGrant(
        subject: String,
        expiration: NoDate,
        val sessionId: String,
        created: NoDate = NoDate(),
        tenant: String = Tenant.NOSTALOGIC.name.lowercase(),
) : NoGrant(
        subject,
        expiration,
        AuthenticationType.LOGIN,
        created = created,
        tenant = tenant,
)
