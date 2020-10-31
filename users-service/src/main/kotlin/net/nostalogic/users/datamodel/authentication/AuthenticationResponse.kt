package net.nostalogic.users.datamodel.authentication

import net.nostalogic.datamodel.NoDate

class AuthenticationResponse(
        val authenticated: Boolean,
        val message: String,
        val token: String? = null,
        val expiration: NoDate? = null)
