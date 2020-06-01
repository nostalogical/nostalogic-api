package net.nostalogic.dtos.responses

import net.nostalogic.datamodel.NoDate

class AuthenticationResponse(val authenticated: Boolean, val message: String,
                             val token: String? = null, val expiration: NoDate? = null)
