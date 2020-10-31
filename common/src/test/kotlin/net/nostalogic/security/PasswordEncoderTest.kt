package net.nostalogic.security

import net.nostalogic.crypto.encoders.EncoderType
import net.nostalogic.crypto.encoders.PasswordEncoder
import net.nostalogic.datamodel.authentication.UserAuthentication
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

    private val encoders: ArrayList<EncoderType> = arrayListOf(EncoderType.SHA512, EncoderType.PBKDF2)

    @Test
    fun `Encoded passwords should be verifiable by the encoder`() {
        for (encoder in encoders) {
            for (password in testPasswords) {
                val auth = PasswordEncoder.encodePassword(password, encoder)
                Assertions.assertTrue(PasswordEncoder.verifyPassword(auth))
            }
        }
    }

    @Test
    fun `Invalid passwords should not be verified by the encoder`() {
        for (encoder in encoders) {
            for (password in testPasswords) {
                val auth = PasswordEncoder.encodePassword(password, encoder)
                val invalidPassword = password + RandomStringUtils.random(5)
                Assertions.assertFalse(PasswordEncoder.verifyPassword(UserAuthentication(invalidPassword, auth.hash, auth.salt, auth.encoder, auth.iterations)))
            }
        }
    }

    @Test
    fun `Empty passwords should fail verification`() {
        for (encoder in encoders) {
            val password = ""
            Assertions.assertThrows(NoValidationException::class.java) {
                PasswordEncoder.encodePassword(password, encoder)
            }
        }
    }

    @Test
    fun `Empty stored hash should fail verification`() {
        val validationString = ""
        for (encoder in encoders) {
            for (password in testPasswords) {
                Assertions.assertFalse(PasswordEncoder.verifyPassword(UserAuthentication(validationString, "", "", EncoderType.PBKDF2)))
            }
        }
    }
}
