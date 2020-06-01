package net.nostalogic.security

import net.nostalogic.BaseApplication
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.datamodel.NoDate
import net.nostalogic.exceptions.NoAccessException
import net.nostalogic.security.grants.LoginGrant
import net.nostalogic.security.utils.JwtUtil
import net.nostalogic.security.utils.TokenDecoder
import net.nostalogic.security.utils.TokenEncoder
import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.temporal.ChronoUnit

@ActiveProfiles(profiles = ["test"])
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [BaseApplication::class])
class JwtTest {

    private val subject = "TestUserId"
    private val groupOne = "groupOne"
    private val groupTwo = "groupTwo"
    private val testEmail = "test@email.com"
    private val sessionId = "TestSessionId"

    @Test
    fun `JWT util should generate unique keys`() {
        val key = JwtUtil.generateJwtKey();
        Assertions.assertNotNull(key)
        Assertions.assertEquals(64, key.length)
        Assertions.assertNotEquals(key, JwtUtil.generateJwtKey())
        val shortKey = JwtUtil.generateJwtKey(12)
        Assertions.assertNotNull(shortKey)
        Assertions.assertEquals(12, shortKey.length)
    }

    @Test
    fun `JWT util should be able to generate variable length keys`() {
        val shortKey = JwtUtil.generateJwtKey(12)
        Assertions.assertNotNull(shortKey)
        Assertions.assertEquals(12, shortKey.length)
    }

    @Test
    fun `Encode and decode a login grant as a JWT token`() {
        val grant = LoginGrant(subject, setOf(groupOne, groupTwo),
                NoDate.plus(5L, ChronoUnit.DAYS), sessionId, AuthenticationType.EMAIL)
        val token = TokenEncoder.encodeLoginGrant(grant)
        Assertions.assertTrue(StringUtils.isNoneBlank(token))
        val decodedGrant = TokenDecoder.decodeToken(token)
        Assertions.assertNotNull(decodedGrant)
        Assertions.assertEquals(grant.created, decodedGrant.created)
        Assertions.assertEquals(grant.expiration, decodedGrant.expiration)
        Assertions.assertEquals(grant.type, decodedGrant.type)
        Assertions.assertEquals(grant.sessionId, (decodedGrant as LoginGrant).sessionId)
        Assertions.assertEquals(grant.additional, decodedGrant.additional)
    }

    @Test
    fun `Attempt to decode an invalid JWT token`() {
        val invalidToken = "invalidToken"
        Assertions.assertThrows(NoAccessException::class.java) {
            TokenDecoder.decodeToken(invalidToken)
        }
    }

    @Test
    fun `Attempt to decode an expired JWT token`() {
        val grant = LoginGrant(subject, setOf(groupOne, groupTwo),
                NoDate.plus(-1L, ChronoUnit.DAYS), sessionId, AuthenticationType.EMAIL)
        val token = TokenEncoder.encodeLoginGrant(grant)
        Assertions.assertTrue(StringUtils.isNoneBlank(token))
        Assertions.assertThrows(NoAccessException::class.java) {
            TokenDecoder.decodeToken(token)
        }
    }

}
