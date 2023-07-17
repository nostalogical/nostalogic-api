package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.Tenant
import net.nostalogic.datamodel.NoDate

class ConfirmationGrant(
        subject: String,
        created: NoDate = NoDate(),
        tenant: String = Tenant.NOSTALOGIC.name.lowercase(),
) : NoGrant(
        subject,
        null,
        AuthenticationType.CONFIRMATION,
        created = created,
        tenant = tenant,
)
