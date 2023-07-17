package net.nostalogic.security.grants

import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.Tenant
import net.nostalogic.datamodel.NoDate

class ImpersonationGrant(
        subject: String,
        expiration: NoDate,
        val sessionId: String,
        val originalSubject: String,
        created: NoDate = NoDate(),
        tenant: String = Tenant.NOSTALOGIC.name.lowercase(),
) : NoGrant(
        subject,
        expiration,
        AuthenticationType.IMPERSONATION,
        created = created,
        tenant = tenant,
)
