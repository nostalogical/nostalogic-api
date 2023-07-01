package net.nostalogic.crypto.encoders

import net.nostalogic.crypto.algorithms.RngAlgorithm
import net.nostalogic.datamodel.authentication.EncodingDetails
import net.nostalogic.datamodel.authentication.UserAuthentication

abstract class PasswordEncoder {

    companion object {

        fun verifyPassword(auth: UserAuthentication): Boolean {
            return if (auth.encoder == EncoderType.PBKDF2)
                PBKDF2Encoder.verifyPassword(auth)
            else
                SHAEncoder.verifyPassword(auth)
        }

        fun encodePassword(password: String, encoderType: EncoderType): UserAuthentication {
            return if (encoderType == EncoderType.PBKDF2)
                PBKDF2Encoder.encodePassword(password)
            else
                SHAEncoder.encodePassword(password)
        }

        fun encodePassword(encodingDetails: EncodingDetails, encoderType: EncoderType): UserAuthentication {
            return if (encoderType == EncoderType.PBKDF2)
                PBKDF2Encoder.encodePassword(encodingDetails)
            else
                SHAEncoder.encodePassword(encodingDetails)
        }

    }

    abstract fun encodePassword(encodingDetails: EncodingDetails): UserAuthentication
    abstract fun verifyPassword(auth: UserAuthentication): Boolean

    fun encodePassword(password: String): UserAuthentication {
        return PBKDF2Encoder.encodePassword(EncodingDetails(password = password))
    }

    fun getSalt(): ByteArray {
        val sr = RngAlgorithm.SHA1PRNG.getSecureRandom()
        val salt = ByteArray(16)
        sr.nextBytes(salt)
        return salt
    }
}
