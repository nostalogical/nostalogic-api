package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.NoDate

/**
 * A refresh grant is long-lived grant that can be used generate a new LoginGrant.
 */
class RefreshGrant (
    subject: String,
    expiration: NoDate,
    val sessionId: String,
    val refreshHash: String,
    created: NoDate = NoDate()
    ) : NoGrant(
    subject,
    expiration,
    AuthenticationType.REFRESH,
    created = created)
