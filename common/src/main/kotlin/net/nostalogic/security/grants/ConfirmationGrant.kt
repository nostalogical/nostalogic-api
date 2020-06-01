package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.NoDate

class ConfirmationGrant(
        subject: String,
        expiration: NoDate,
        created: NoDate = NoDate()
) : NoGrant(
        subject,
        expiration,
        AuthenticationType.REG_CONFIRM,
        created = created
)
