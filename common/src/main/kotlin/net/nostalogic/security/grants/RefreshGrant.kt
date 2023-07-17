package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.Tenant
import net.nostalogic.datamodel.NoDate

/**
 * A refresh grant is long-lived grant that can be used generate a new LoginGrant.
 */
class RefreshGrant (
    subject: String,
    expiration: NoDate,
    val sessionId: String,
    val refreshHash: String,
    created: NoDate = NoDate(),
    tenant: String = Tenant.NOSTALOGIC.name.lowercase(),
    ) : NoGrant(
    subject,
    expiration,
    AuthenticationType.REFRESH,
    created = created,
    tenant = tenant
    )
