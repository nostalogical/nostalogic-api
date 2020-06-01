package net.nostalogic.crypto.encoders

import net.nostalogic.crypto.algorithms.RngAlgorithm
import org.apache.commons.lang3.StringUtils

abstract class PasswordEncoder {

    protected val passwordSeparator = "::"

    abstract fun encodePassword(password: String): String
    abstract fun verifyPassword(password: String, storedString: String): Boolean

    fun getSalt(): ByteArray {
        val sr = RngAlgorithm.SHA1PRNG.getSecureRandom()
        val salt = ByteArray(16)
        sr.nextBytes(salt)
        return salt
    }

    fun validPasswordFormat(password: String): Boolean {
        return StringUtils.isNotEmpty(password)
    }
}
