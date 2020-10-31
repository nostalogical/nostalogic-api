package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.NoDate

class ConfirmationGrant(
        subject: String,
        created: NoDate = NoDate()
) : NoGrant(
        subject,
        null,
        AuthenticationType.REG_CONFIRM,
        created = created
)
