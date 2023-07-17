package net.nostalogic.security.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.*

object JwtUtil {

    private val logger = LoggerFactory.getLogger(JwtUtil::class.java)

    const val KEY_PROPERTY = "security.jwt.key"
    private const val ISSUER = "nostate"
    private var signingAlgorithm: Algorithm = Algorithm.HMAC512("placeholder")
    private var tokenVerifier: JWTVerifier = createVerifier(signingAlgorithm)

    fun setKey(key: String) {
        signingAlgorithm = Algorithm.HMAC512(key)
        tokenVerifier = createVerifier(signingAlgorithm)
    }

    private fun createVerifier(algorithm: Algorithm) : JWTVerifier {
        return JWT.require(algorithm).withIssuer(ISSUER).build()
    }

    fun generateJwtKey(length: Int = 64) : String {
        return RandomStringUtils.random(length, true, false)
    }

    fun getTokenBuilder() : JWTCreator.Builder {
        return JWT.create()
    }

    fun signTokenBuilder(builder: JWTCreator.Builder): String {
        return Base64.getEncoder().encodeToString(builder
                .withIssuer(ISSUER)
                .sign(signingAlgorithm)
                .toByteArray())
    }

    fun verifyJwtToken(token: String): DecodedJWT? {
        if (StringUtils.isEmpty(token))
            return null
        val decodedJWT : DecodedJWT?
        try {
            val decodedTokenString = String(Base64.getDecoder().decode(removeBearer(token)))
            decodedJWT =  tokenVerifier.verify(decodedTokenString)
        } catch (e: Exception) {
            logger.error("Error encountered while attempting to verify token: ${token}", e)
            return null
        }
        return decodedJWT
    }

    private fun removeBearer(token: String): String {
        return token.replace(regex = "(?i)bearer\\s*".toRegex(), replacement = "")
    }
}
