package net.nostalogic.security

import net.nostalogic.crypto.encoders.PBKDF2Encoder
import net.nostalogic.crypto.encoders.PasswordEncoder
import net.nostalogic.crypto.encoders.SHAEncoder
import net.nostalogic.exceptions.NoValidationException
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles(profiles = ["test"])
class PasswordEncoderTest {

    private val testPasswords = arrayListOf("net/nostalogic/security/password", "TestPassword567", "pnfb93~'a;]",
            "WordBasedPass!4", "01s12';{fb@]6[=")

    private val encoders: ArrayList<PasswordEncoder> = arrayListOf(SHAEncoder, PBKDF2Encoder)

    @Test
    fun `Encoded passwords should be verifiable by the encoder`() {
        for (encoder in encoders) {
            for (password in testPasswords) {
                val validationString = encoder.encodePassword(password)
                Assertions.assertTrue(encoder.verifyPassword(password, validationString))
            }
        }
    }

    @Test
    fun `Invalid passwords should not be verified by the encoder`() {
        for (encoder in encoders) {
            for (password in testPasswords) {
                val validationString = encoder.encodePassword(password)
                val invalidPassword = password + RandomStringUtils.random(5)
                Assertions.assertFalse(encoder.verifyPassword(invalidPassword, validationString))
            }
        }
    }

    @Test
    fun `Empty passwords should fail verification`() {
        for (encoder in encoders) {
            val password = ""
            Assertions.assertThrows(NoValidationException::class.java) {
                encoder.encodePassword(password)
            }
        }
    }

    @Test
    fun `Empty stored hash should fail verification`() {
        val validationString = ""
        for (encoder in encoders) {
            for (password in testPasswords) {
                Assertions.assertFalse(encoder.verifyPassword(password, validationString))
            }
        }
    }
}
