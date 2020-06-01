package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.NoDate

/**
 * Used to bypass the access service and mock rights for tests. Should never be generated outside a test profile.
 */
class TestGrant(
        subject: String,
        expiration: NoDate,
        created: NoDate = NoDate()
) : NoGrant(
        subject,
        expiration,
        AuthenticationType.TEST,
        created = created
)
