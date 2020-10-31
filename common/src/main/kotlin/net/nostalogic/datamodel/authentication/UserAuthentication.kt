package net.nostalogic.datamodel.authentication

import net.nostalogic.crypto.encoders.EncoderType

class UserAuthentication(
        val password: String,
        val hash: String,
        val salt: String,
        val encoder: EncoderType,
        val iterations: Int = 1
)
