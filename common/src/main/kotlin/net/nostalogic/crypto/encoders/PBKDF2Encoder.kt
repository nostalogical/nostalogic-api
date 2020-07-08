package net.nostalogic.crypto.encoders

import net.nostalogic.crypto.algorithms.SecretKeyAlgorithm
import net.nostalogic.exceptions.NoValidationException
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import javax.crypto.spec.PBEKeySpec

object PBKDF2Encoder : PasswordEncoder() {

    private val logger = LoggerFactory.getLogger(PBKDF2Encoder::class.java)
    private const val ITERATIONS = 1422
    private const val HASH_LENGTH = 64 * 8

    override fun encodePassword(password: String): String {
        if (StringUtils.isEmpty(password))
            throw NoValidationException(107003, "net.nostalogic.security.password", "Password cannot be empty")

        val salt = SHAEncoder.getSalt()
        val passwordBytes = createHash(password, salt, HASH_LENGTH)

        return ITERATIONS.toString() +
                passwordSeparator + HexEncoder.bytesToHex(salt) +
                passwordSeparator + HexEncoder.bytesToHex(passwordBytes)
    }

    override fun verifyPassword(password: String, storedString: String): Boolean {
        var verified = StringUtils.isNotEmpty(password)

        if (verified) {
            val parts = storedString.split(passwordSeparator)
            if (parts.size != 3) {
                logger.error("Invalid verification data: Expected 3 parts, found " + parts.size)
                return false
            }
            val salt = HexEncoder.hexToBytes(parts[1])
            val hash = HexEncoder.hexToBytes(parts[2])
            val validationHash = createHash(password, salt, hash.size * 8)

            var diff = hash.size xor validationHash.size
            val limit = Math.min(hash.size, validationHash.size)
            for (i in 0 until limit)
                diff = (diff xor (hash[i].toInt() xor validationHash[i].toInt()))

            verified = diff == 0
        }

        return verified
    }

    private fun createHash(password: String, salt: ByteArray, length: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, length)
        val skf = SecretKeyAlgorithm.PBKDF2WithHmacSHA1.getSecretFactory()
        return skf.generateSecret(spec).encoded
    }
}
