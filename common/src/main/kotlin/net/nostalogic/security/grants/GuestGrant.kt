package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.NoStrings
import net.nostalogic.constants.Tenant
import net.nostalogic.datamodel.NoDate

/**
 * A guest grant represents any anonymous user or bot not currently logged in.
 */
class GuestGrant(
        tenant: String = Tenant.NOSTALOGIC.name.lowercase()
) : NoGrant(
        subject = NoStrings.AUTH_GUEST,
        expiration = NoDate(),
        type = AuthenticationType.GUEST,
        created = NoDate(),
        tenant = tenant,
)
