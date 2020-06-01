package net.nostalogic.security.utils

import net.nostalogic.security.grants.ImpersonationGrant
import net.nostalogic.security.grants.LoginGrant

object TokenEncoder {

    const val DESCRIPTION = "desc"
    const val SESSION = "ses"
    const val GRANT_TYPE = "gt"
    const val ADDITIONAL_SUBJECTS = "add"
    const val ORIGINAL_USER = "ou"
    const val ALTERNATE_IMPERSONATIONS = "ai"

    fun encodeLoginGrant(grant: LoginGrant): String {
        return JwtUtil.signTokenBuilder(JwtUtil.getTokenBuilder()
                .withSubject(grant.subject)
                .withIssuedAt(grant.created.getDate())
                .withExpiresAt(grant.expiration.getDate())
                .withClaim(GRANT_TYPE, grant.type.name)
                .withClaim(DESCRIPTION, grant.description)
                .withClaim(SESSION, grant.sessionId)
                .withArrayClaim(ADDITIONAL_SUBJECTS, grant.additional.toTypedArray()))
    }

    fun encodeImpersonationGrant(grant: ImpersonationGrant): String {
        return JwtUtil.signTokenBuilder(JwtUtil.getTokenBuilder()
                .withSubject(grant.subject)
                .withIssuedAt(grant.created.getDate())
                .withExpiresAt(grant.expiration.getDate())
                .withClaim(GRANT_TYPE, grant.type.name)
                .withClaim(DESCRIPTION, grant.description)
                .withClaim(SESSION, grant.sessionId)
                .withArrayClaim(ADDITIONAL_SUBJECTS, grant.additional.toTypedArray())
                .withArrayClaim(ALTERNATE_IMPERSONATIONS, grant.alternateSubjects.toTypedArray())
                .withClaim(ORIGINAL_USER, grant.originalSubject))
    }
}
