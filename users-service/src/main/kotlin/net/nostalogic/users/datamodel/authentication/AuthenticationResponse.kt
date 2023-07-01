package net.nostalogic.users.datamodel.authentication

import net.nostalogic.datamodel.NoDate
import net.nostalogic.security.models.TokenDetails

class AuthenticationResponse(
        val authenticated: Boolean,
        val message: String,
        val accessToken: TokenDetails? = null,
        val refreshToken: TokenDetails? = null)
