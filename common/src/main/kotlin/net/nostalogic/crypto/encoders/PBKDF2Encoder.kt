package net.nostalogic.crypto.encoders

import net.nostalogic.crypto.algorithms.SecretKeyAlgorithm
import net.nostalogic.datamodel.authentication.UserAuthentication
import net.nostalogic.exceptions.NoValidationException
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.random.Random

object PBKDF2Encoder : PasswordEncoder() {

    private val log = LoggerFactory.getLogger(PBKDF2Encoder::class.java)
    private const val HASH_LENGTH = 64 * 8
    private val PEPPER = HexEncoder.hexToBytes("c68b32da7a33063572f351939e058a5d")

    override fun encodePassword(password: String): UserAuthentication {
        if (StringUtils.isEmpty(password))
            throw NoValidationException(107003, "password", "Password cannot be empty")

        val salt = SHAEncoder.getSalt()
        val iterations = Random.nextInt(1000, 1500)
        val passwordBytes = createHash(password, salt, HASH_LENGTH, iterations)

        return UserAuthentication(
                password = password,
                hash = HexEncoder.bytesToHex(passwordBytes),
                salt = HexEncoder.bytesToHex(salt),
                encoder = EncoderType.PBKDF2,
                iterations = iterations)
    }

    override fun verifyPassword(auth: UserAuthentication): Boolean {

        if (StringUtils.isNotEmpty(auth.password)) {
            val salt = HexEncoder.hexToBytes(auth.salt)
            val hash = HexEncoder.hexToBytes(auth.hash)
            val validationHash = createHash(auth.password, salt, hash.size * 8, auth.iterations)

            var diff = hash.size xor validationHash.size
            val limit = Math.min(hash.size, validationHash.size)
            for (i in 0 until limit)
                diff = (diff xor (hash[i].toInt() xor validationHash[i].toInt()))

            return diff == 0
        }

        return false
    }

    private fun createHash(password: String, salt: ByteArray, length: Int, iteration: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt + PEPPER, iteration, length)
        val skf = SecretKeyAlgorithm.PBKDF2WithHmacSHA1.getSecretFactory()
        return skf.generateSecret(spec).encoded
    }
}
