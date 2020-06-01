package net.nostalogic.crypto.encoders

import net.nostalogic.crypto.algorithms.MessageDigestAlgorithm
import net.nostalogic.exceptions.NoValidationException
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

object SHAEncoder : PasswordEncoder() {

    private val logger = LoggerFactory.getLogger(SHAEncoder::class.java)

    override fun encodePassword(password: String): String {
        if (StringUtils.isEmpty(password))
            throw NoValidationException(106001, "net.nostalogic.security.password", "Password cannot be empty", null)

        val salt = getSalt()
        val passwordBytes = createHash(password, salt)

        return HexEncoder.bytesToHex(salt) + passwordSeparator + HexEncoder.bytesToHex(passwordBytes)
    }

    override fun verifyPassword(password: String, storedString: String): Boolean {
        var verified = StringUtils.isNotEmpty(password)

        if (verified) {
            val parts = storedString.split(passwordSeparator)
            if (parts.size != 2) {
                logger.error("Invalid verification data: Expected 2 parts, found " + parts.size)
                return false
            }
            val salt = HexEncoder.hexToBytes(parts[0])
            val hash = HexEncoder.hexToBytes(parts[1])
            val validationHash = createHash(password, salt)

            var diff = hash.size xor validationHash.size
            val limit = Math.min(hash.size, validationHash.size)
            for (i in 0 until limit)
                diff = (diff xor (hash[i].toInt() xor validationHash[i].toInt()))

            verified = diff == 0
        }

        return verified
    }

    private fun createHash(password: String, salt: ByteArray) : ByteArray {
        val md = MessageDigestAlgorithm.SHA512.getDigest()
        md.update(salt)
        return md.digest(password.toByteArray())
    }
}
