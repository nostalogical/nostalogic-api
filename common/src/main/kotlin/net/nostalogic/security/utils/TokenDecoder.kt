package net.nostalogic.security.utils

import com.auth0.jwt.interfaces.DecodedJWT
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.NoDate
import net.nostalogic.exceptions.NoAuthException
import net.nostalogic.security.grants.*

object TokenDecoder {

    fun decodeToken(token: String): NoGrant {
        val decodedJWT = JwtUtil.verifyJwtToken(token) ?: throw NoAuthException(102001,
                "The supplied token cannot be verified or decoded")
        return when (decodedJWT.getClaim(TokenEncoder.GRANT_TYPE).asString()) {
            AuthenticationType.LOGIN.name -> decodeLoginToken(decodedJWT)
            AuthenticationType.REFRESH.name -> decodeRefreshToken(decodedJWT)
            AuthenticationType.IMPERSONATION.name -> decodeImpersonationToken(decodedJWT)
            AuthenticationType.CONFIRMATION.name -> decodeConfirmationToken(decodedJWT)
            AuthenticationType.PASSWORD_RESET.name -> decodePasswordResetToken(decodedJWT)
            else -> throw NoAuthException(102002, "The supplied token has an unknown authentication type")
        }

    }

    private fun decodeLoginToken(jwt: DecodedJWT): LoginGrant {
        return LoginGrant(
            jwt.subject,
            NoDate(jwt.expiresAt),
            jwt.getClaim(TokenEncoder.SESSION).asString(),
            created = NoDate(jwt.issuedAt),
            tenant = jwt.getClaim(TokenEncoder.TENANT).asString(),
        )
    }

    private fun decodeRefreshToken(jwt: DecodedJWT): RefreshGrant {
        return RefreshGrant(
            jwt.subject,
            NoDate(jwt.expiresAt),
            jwt.getClaim(TokenEncoder.SESSION).asString(),
            created = NoDate(jwt.issuedAt),
            refreshHash = jwt.getClaim(TokenEncoder.TOKEN_HASH).asString(),
            tenant = jwt.getClaim(TokenEncoder.TENANT).asString(),
        )
    }

    private fun decodeImpersonationToken(jwt: DecodedJWT): ImpersonationGrant {
        return ImpersonationGrant(
            jwt.subject,
            NoDate(jwt.expiresAt),
            jwt.getClaim(TokenEncoder.SESSION).asString(),
            jwt.getClaim(TokenEncoder.ORIGINAL_USER).asString(),
            created = NoDate(jwt.issuedAt),
            tenant = jwt.getClaim(TokenEncoder.TENANT).asString(),
        )
    }

    private fun decodeConfirmationToken(jwt: DecodedJWT): ConfirmationGrant {
        return ConfirmationGrant(
            jwt.subject,
            created = NoDate(jwt.issuedAt),
            tenant = jwt.getClaim(TokenEncoder.TENANT).asString(),
        )
    }

    private fun decodePasswordResetToken(jwt: DecodedJWT): PasswordResetGrant {
        return PasswordResetGrant(
            jwt.subject,
            created = NoDate(jwt.issuedAt),
            tenant = jwt.getClaim(TokenEncoder.TENANT).asString(),
        )
    }
}
