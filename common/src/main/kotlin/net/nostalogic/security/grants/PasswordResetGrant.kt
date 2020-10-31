package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.NoDate
import java.time.temporal.ChronoUnit

class PasswordResetGrant(
        subject: String,
        created: NoDate = NoDate()
) : NoGrant(
        subject,
        created.addTime(7, ChronoUnit.DAYS),
        AuthenticationType.PASSWORD_RESET,
        created = created
)
