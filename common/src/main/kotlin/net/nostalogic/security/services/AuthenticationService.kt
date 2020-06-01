package net.nostalogic.security.services

import net.nostalogic.config.i18n.Translator
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.SessionEvent
import net.nostalogic.crypto.encoders.PBKDF2Encoder
import net.nostalogic.dtos.requests.LoginRequest
import net.nostalogic.dtos.responses.AuthenticationResponse
import net.nostalogic.security.contexts.AuthenticationContext
import net.nostalogic.security.models.SessionPrompt
import java.util.regex.Pattern

object AuthenticationService {

    private val PASSWORD_ENCODER = PBKDF2Encoder
    private val EMAIL_PATTERN = Pattern.compile("\\w+@\\w+\\.\\w+")

    /**
     * Login process:
     * Username/email and net.nostalogic.security.password is sent to the users microservice
     * Username/email is matched to a user ID in the database
     * Password is validated against the DB entry
     * User ID and authentication method is sent to the access service
     * Access service creates a session entry and the token
     * AuthenticationResponse is returned to the user from the access service via the user service
     */

    fun generateLoginContext(request: LoginRequest): AuthenticationContext {
        return AuthenticationContext(if(isEmail(request.username)) AuthenticationType.EMAIL else AuthenticationType.USERNAME)
    }

    fun authenticateLoginContext(context: AuthenticationContext): AuthenticationResponse {
        val errorResponse = AuthenticationResponse(false, Translator.translate("passwordNotVerified"))
        // validate net.nostalogic.security.password

        if (!context.isVerifiable() || !PASSWORD_ENCODER.verifyPassword(context.password.orEmpty(), context.storedPassword.orEmpty()))
            return errorResponse
//        val prompt = SessionPrompt(context.userId.orEmpty(), SessionEvent.LOGIN)

        //Need to be able to call access service to proceed

//        val grant = LoginGrant(context.userId.orEmpty(), context.additionalSubjects, )
        return AuthenticationResponse(false, "Not yet implemented")
    }

    fun isEmail(usernameOrEmail: String): Boolean {
        return EMAIL_PATTERN.matcher(usernameOrEmail).find()
    }

    fun verifyPassword(): Boolean {
        return PASSWORD_ENCODER.verifyPassword("", "")
    }

}
