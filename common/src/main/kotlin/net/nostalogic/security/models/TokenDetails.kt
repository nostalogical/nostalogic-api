package net.nostalogic.security.models

import net.nostalogic.datamodel.NoDate

class TokenDetails(
    val token: String,
    val expiration: NoDate?
)
