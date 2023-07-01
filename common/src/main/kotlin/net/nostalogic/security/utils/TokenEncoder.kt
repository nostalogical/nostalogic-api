package net.nostalogic.security.utils

import net.nostalogic.security.grants.*

object TokenEncoder {

    // Claim names are abbreviated to keep token size down
    const val DESCRIPTION = "desc"
    const val SESSION = "ses"
    const val GRANT_TYPE = "gt"
    const val ORIGINAL_USER = "ou"
    const val TOKEN_HASH = "h"

    fun encodeLoginGrant(grant: LoginGrant): String {
        return JwtUtil.signTokenBuilder(JwtUtil.getTokenBuilder()
                .withSubject(grant.subject)
                .withIssuedAt(grant.created.getDate())
                .withExpiresAt(grant.expiration!!.getDate())
                .withClaim(GRANT_TYPE, grant.type.name)
                .withClaim(DESCRIPTION, grant.description)
                .withClaim(SESSION, grant.sessionId)
        )
    }

    fun encodeRefreshGrant(grant: RefreshGrant): String {
        return JwtUtil.signTokenBuilder(JwtUtil.getTokenBuilder()
                .withSubject(grant.subject)
                .withIssuedAt(grant.created.getDate())
                .withExpiresAt(grant.expiration!!.getDate())
                .withClaim(GRANT_TYPE, grant.type.name)
                .withClaim(DESCRIPTION, grant.description)
                .withClaim(SESSION, grant.sessionId)
                .withClaim(TOKEN_HASH, grant.refreshHash)
        )
    }

    fun encodeImpersonationGrant(grant: ImpersonationGrant): String {
        return JwtUtil.signTokenBuilder(JwtUtil.getTokenBuilder()
                .withSubject(grant.subject)
                .withIssuedAt(grant.created.getDate())
                .withExpiresAt(grant.expiration!!.getDate())
                .withClaim(GRANT_TYPE, grant.type.name)
                .withClaim(DESCRIPTION, grant.description)
                .withClaim(SESSION, grant.sessionId)
                .withClaim(ORIGINAL_USER, grant.originalSubject)
        )
    }

    fun encodeRegistrationGrant(grant: ConfirmationGrant): String {
        return JwtUtil.signTokenBuilder(JwtUtil.getTokenBuilder()
                .withSubject(grant.subject)
                .withIssuedAt(grant.created.getDate())
                .withClaim(GRANT_TYPE, grant.type.name)
                .withClaim(DESCRIPTION, grant.description))
    }

    fun encodePasswordResetGrant(grant: PasswordResetGrant): String {
        return JwtUtil.signTokenBuilder(JwtUtil.getTokenBuilder()
                .withSubject(grant.subject)
                .withIssuedAt(grant.created.getDate())
                .withExpiresAt(grant.expiration!!.getDate())
                .withClaim(GRANT_TYPE, grant.type.name)
                .withClaim(DESCRIPTION, grant.description)
        )
    }
}
