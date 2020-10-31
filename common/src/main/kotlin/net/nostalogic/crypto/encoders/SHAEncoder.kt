package net.nostalogic.crypto.encoders

import net.nostalogic.crypto.algorithms.MessageDigestAlgorithm
import net.nostalogic.datamodel.authentication.UserAuthentication
import net.nostalogic.exceptions.NoValidationException
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

object SHAEncoder : PasswordEncoder() {

    private val log = LoggerFactory.getLogger(SHAEncoder::class.java)
    private val PEPPER = HexEncoder.hexToBytes("98f207d6b0d9acdaf16b54812a0dc0fa")

    override fun encodePassword(password: String): UserAuthentication {
        if (StringUtils.isEmpty(password))
            throw NoValidationException(107001, "password", "Password cannot be empty")

        val salt = getSalt()
        val passwordBytes = createHash(password, salt)

        return UserAuthentication(
                password = password,
                hash = HexEncoder.bytesToHex(passwordBytes),
                salt = HexEncoder.bytesToHex(salt),
                encoder = EncoderType.SHA512
        )
    }

    override fun verifyPassword(auth: UserAuthentication): Boolean {

        if (StringUtils.isNotEmpty(auth.password)) {
            val salt = HexEncoder.hexToBytes(auth.salt)
            val hash = HexEncoder.hexToBytes(auth.hash)
            val validationHash = createHash(auth.password, salt)

            var diff = hash.size xor validationHash.size
            val limit = Math.min(hash.size, validationHash.size)
            for (i in 0 until limit)
                diff = (diff xor (hash[i].toInt() xor validationHash[i].toInt()))

            return diff == 0
        }

        return false
    }

    private fun createHash(password: String, salt: ByteArray) : ByteArray {
        val md = MessageDigestAlgorithm.SHA512.getDigest()
        md.update(salt)
        return md.digest(password.toByteArray())
    }
}
