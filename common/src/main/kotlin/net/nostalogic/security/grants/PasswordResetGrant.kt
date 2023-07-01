package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.NoDate
import java.time.temporal.ChronoUnit

class PasswordResetGrant(
        subject: String,
        expiration: NoDate = NoDate().addTime(7, ChronoUnit.DAYS),
        created: NoDate = NoDate()
) : NoGrant(
        subject,
        expiration,
        AuthenticationType.PASSWORD_RESET,
        created = created
)
