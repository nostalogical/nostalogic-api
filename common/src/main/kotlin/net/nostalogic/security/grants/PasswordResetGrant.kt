package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.Tenant
import net.nostalogic.datamodel.NoDate
import java.time.temporal.ChronoUnit

class PasswordResetGrant(
        subject: String,
        expiration: NoDate = NoDate().addTime(7, ChronoUnit.DAYS),
        created: NoDate = NoDate(),
        tenant: String = Tenant.NOSTALOGIC.name.lowercase(),
) : NoGrant(
        subject,
        expiration,
        AuthenticationType.PASSWORD_RESET,
        created = created,
        tenant = tenant,
)
