package net.nostalogic.security.utils

import com.auth0.jwt.interfaces.DecodedJWT
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.NoDate
import net.nostalogic.exceptions.NoAuthException
import net.nostalogic.security.grants.ImpersonationGrant
import net.nostalogic.security.grants.LoginGrant
import net.nostalogic.security.grants.NoGrant

object TokenDecoder {

    fun decodeToken(token: String): NoGrant {
        val decodedJWT = JwtUtil.verifyJwtToken(token) ?: throw NoAuthException(102001,
                "The supplied token cannot be verified or decoded")
        return when (decodedJWT.getClaim(TokenEncoder.GRANT_TYPE).asString()) {
            AuthenticationType.USERNAME.name, AuthenticationType.EMAIL.name -> decodeLoginToken(decodedJWT)
            AuthenticationType.IMPERSONATION.name -> decodeImpersonationToken(decodedJWT)
            else -> throw NoAuthException(102002, "The supplied token has an unknown authentication type")
        }

    }

    private fun decodeLoginToken(jwt: DecodedJWT): LoginGrant {
        return LoginGrant(
                jwt.subject,
                jwt.getClaim(TokenEncoder.ADDITIONAL_SUBJECTS).asArray(String::class.java).toSet(),
                NoDate(jwt.expiresAt),
                jwt.getClaim(TokenEncoder.SESSION).asString(),
                AuthenticationType.valueOf(jwt.getClaim(TokenEncoder.GRANT_TYPE).asString()),
                created = NoDate(jwt.issuedAt)
        )
    }

    private fun decodeImpersonationToken(jwt: DecodedJWT): ImpersonationGrant {
        return ImpersonationGrant(
                jwt.subject,
                jwt.getClaim(TokenEncoder.ADDITIONAL_SUBJECTS).asArray(String::class.java).toSet(),
                NoDate(jwt.expiresAt),
                jwt.getClaim(TokenEncoder.SESSION).asString(),
                jwt.getClaim(TokenEncoder.ORIGINAL_USER).asString(),
                jwt.getClaim(TokenEncoder.ALTERNATE_IMPERSONATIONS).asArray(String::class.java).toSet(),
                created = NoDate(jwt.issuedAt)
        )
    }
}
