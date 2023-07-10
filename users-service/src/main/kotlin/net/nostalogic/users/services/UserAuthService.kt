package net.nostalogic.users.services

import net.nostalogic.comms.Comms
import net.nostalogic.comms.ExcommComms
import net.nostalogic.config.i18n.Translator
import net.nostalogic.constants.*
import net.nostalogic.crypto.encoders.EncoderType
import net.nostalogic.crypto.encoders.PasswordEncoder
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.datamodel.authentication.UserAuthentication
import net.nostalogic.datamodel.excomm.MessageOutline
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.*
import net.nostalogic.security.contexts.SessionContext
import net.nostalogic.security.grants.ImpersonationGrant
import net.nostalogic.security.grants.LoginGrant
import net.nostalogic.security.grants.PasswordResetGrant
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.security.models.SessionSummary
import net.nostalogic.security.utils.TokenEncoder
import net.nostalogic.users.datamodel.authentication.AuthenticationResponse
import net.nostalogic.users.datamodel.authentication.ImpersonationRequest
import net.nostalogic.users.datamodel.authentication.LoginRequest
import net.nostalogic.users.mappers.AuthMapper
import net.nostalogic.users.persistence.entities.AuthenticationEntity
import net.nostalogic.users.persistence.entities.UserEntity
import net.nostalogic.users.persistence.repositories.AuthenticationRepository
import net.nostalogic.users.persistence.repositories.UserRepository
import net.nostalogic.users.validators.LoginValidator
import net.nostalogic.users.validators.PasswordValidator
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.regex.Pattern

@Service
class UserAuthService(
    @Autowired private val userRepository: UserRepository,
    @Autowired private val authRepository: AuthenticationRepository,
    ) {

    private val logger = LoggerFactory.getLogger(UserAuthService::class.java)
    private val EMAIL_PATTERN = Pattern.compile("\\w+@\\w+\\.\\w+")

    fun login(loginRequest: LoginRequest): AuthenticationResponse {
        val userEntity = getUserFromLogin(loginRequest)
            ?: return createStandardLoginResponse()

        val authEntity = getAuthForUser(userEntity)
        if (authEntity == null) {
            logger.error("No authentication entries found for user ${userEntity.id}")
            return createStandardLoginResponse()
        }

        val loggedIn = PasswordEncoder.verifyPassword(UserAuthentication(
                password = loginRequest.password!!,
                hash = authEntity.hash,
                salt = authEntity.salt,
                encoder = authEntity.encoder,
                iterations = authEntity.iterations))

        if (!loggedIn) return createStandardLoginResponse()

        if (authEntity.invalid || authEntity.expired) {
            val errorString = if (authEntity.invalid) ErrorStrings.PASSWORD_INVALID else ErrorStrings.PASSWORD_EXPIRED
            return AuthenticationResponse(authenticated = false, message = Translator.translate(errorString))
        }

        val loginSource =
            if (EMAIL_PATTERN.matcher(loginRequest.username!!).find()) AuthenticationSource.EMAIL
            else AuthenticationSource.USERNAME
        val session = Comms.accessComms.createSession(SessionPrompt(userEntity.id, loginSource))
                ?: throw NoAccessException(302006, "Failed to create session internally", Translator.translate(ErrorStrings.COMMS_ERROR))

        SessionContext.getToken()?.let { Comms.accessComms.endSession(it) }

        return createStandardLoginResponse(true, session)
    }

    private fun createStandardLoginResponse(loggedIn: Boolean = false, session: SessionSummary? = null): AuthenticationResponse {
        return AuthenticationResponse(
            authenticated = loggedIn,
            message = if (loggedIn) NoStrings.authGranted() else NoStrings.passwordNotVerified(),
            accessToken = session?.accessToken,
            refreshToken = session?.refreshToken,
            )
    }

    fun logout(): AuthenticationResponse {
        val token = SessionContext.getToken()
        if (token != null)
            Comms.accessComms.endSession(token)
        return AuthenticationResponse(
                authenticated = false,
                message = if (token != null) NoStrings.authInvalidated() else NoStrings.authAlreadyInvalid())
    }

    fun refresh(): AuthenticationResponse {
        val token = SessionContext.getToken()
        return if (token == null) {
            AuthenticationResponse(authenticated = false, message = NoStrings.sessionRefreshFail())
        } else {
            val summary = Comms.accessComms.refreshSession(token)
                    ?: throw NoAccessException(302007, "Failed to refresh session internally", NoStrings.authRefreshDenied())
            val refreshed = summary.accessToken != null
            AuthenticationResponse(
                    authenticated = refreshed,
                    message = if (refreshed) NoStrings.authGranted() else NoStrings.authRefreshDenied(),
                    accessToken = summary.accessToken,
                    refreshToken = summary.refreshToken
            )
        }
    }

    /**
     * Handles resetting a password with no credentials (trigger a reset email), with the token from an email
     * (PasswordResetGrant), or an existing valid password.
     */
    fun resetPassword(loginRequest: LoginRequest): AuthenticationResponse {
        val grant = SessionContext.getGrant()
        if (grant is PasswordResetGrant)
            return createNewPasswordWithGrant(loginRequest, grant)

        // This response will be untrue if the user doesn't exist, but it prevents user enumeration
        val userEntity = getUserFromLogin(loginRequest, requirePassword = false)
            ?: return AuthenticationResponse(false, NoStrings.passwordEmailSent())
        val authEntity = getAuthForUser(userEntity)


        return if (StringUtils.isNotBlank(loginRequest.newPassword) && (authEntity == null || authEntity.invalid))
            AuthenticationResponse(false, Translator.translate(ErrorStrings.PASSWORD_INVALID))
        else if (StringUtils.isNotBlank(loginRequest.password) && StringUtils.isNotBlank(loginRequest.newPassword)) {
            createNewPasswordWithExpiredPassword(loginRequest, userEntity, authEntity!!)
        } else {
            sendPasswordResetEmail(userEntity)
        }
    }

    private fun createNewPasswordWithGrant(loginRequest: LoginRequest, grant: PasswordResetGrant): AuthenticationResponse {
        val userEntity = userRepository.findByIdEquals(grant.subject)
                ?: throw NoAuthException(302004, "Password reset token cannot be verified")

        PasswordValidator.validate(loginRequest.newPassword)
        authRepository.findTopByUserIdEqualsOrderByCreatedDesc(userEntity.id)?.let { expirePassword(it, true, "A new password was set") }
        setNewPassword(userEntity.id, loginRequest.newPassword)

        val session = Comms.accessComms.createSession(
            SessionPrompt(
                userEntity.id,
                AuthenticationSource.PASSWORD_RESET,
                reset = true
            )
        )
                ?: throw NoAccessException(302011, "Failed to create session internally", Translator.translate(ErrorStrings.COMMS_ERROR))
        return AuthenticationResponse(
                authenticated = session.accessToken != null,
                message = if (session.accessToken != null) NoStrings.passwordChanged() else NoStrings.sessionCreateFail(),
                accessToken = session.accessToken,
                refreshToken = session.refreshToken
        )
    }

    private fun sendPasswordResetEmail(userEntity: UserEntity): AuthenticationResponse {
        ExcommComms.send(MessageOutline(
                recipientId = userEntity.id,
                recipientEmailAddress = userEntity.email,
                type = MessageType.PASSWORD_RESET,
                locale = userEntity.locale)
                .setParameter("reset_code", TokenEncoder.encodePasswordResetGrant(PasswordResetGrant(userEntity.id))))
        return AuthenticationResponse(false, NoStrings.passwordEmailSent())
    }

    private fun createNewPasswordWithExpiredPassword(loginRequest: LoginRequest, userEntity: UserEntity, authEntity: AuthenticationEntity): AuthenticationResponse {
        if (PasswordEncoder.verifyPassword(UserAuthentication(
                        password = loginRequest.password!!,
                        hash = authEntity.hash,
                        salt = authEntity.salt,
                        encoder = authEntity.encoder,
                        iterations = authEntity.iterations))) {
            setNewPassword(userEntity.id, loginRequest.newPassword)
            expirePassword(authEntity, true, "A new password was set")

            val loginSource =
                if (EMAIL_PATTERN.matcher(loginRequest.username!!).find()) AuthenticationSource.EMAIL
                else AuthenticationSource.USERNAME
            val session = Comms.accessComms.createSession(SessionPrompt(
                userEntity.id,
                loginSource,
                reset = true
            ))
                    ?: throw NoAccessException(302011, "Failed to create session internally", Translator.translate(ErrorStrings.COMMS_ERROR))

            return AuthenticationResponse(
                    authenticated = session.accessToken != null,
                    message = if (session.accessToken != null) NoStrings.passwordChanged() else NoStrings.sessionCreateFail(),
                    accessToken = session.accessToken,
                    refreshToken = session.refreshToken
            )
        } else
            return AuthenticationResponse(
                    authenticated = false,
                    message = NoStrings.passwordNotVerified())
    }

    fun impersonate(impRequest: ImpersonationRequest): AuthenticationResponse {
        val grant = SessionContext.getGrant()
        val originalUserId = when (grant) {
            is LoginGrant -> grant.subject
            is ImpersonationGrant -> grant.originalSubject
            else -> throw NoAccessException(301002, "Only a logged in user can impersonate another user", NoStrings.impersonationDenied())
        }
        val alternates = HashSet<String>()

        if (impRequest.userId == originalUserId)
            throw NoAccessException(302009, "A user cannot impersonate themselves", NoStrings.impersonationDenied())
        if (!AccessQuery().simpleCheck(id = impRequest.userId, entity = NoEntity.USER, action = PolicyAction.EDIT))
            throw NoAccessException(302010, "Missing edit permission for this user, which is required for impersonation", NoStrings.impersonationDenied())

        val session = Comms.accessComms.createSession(SessionPrompt(
                userId = impRequest.userId,
                type = AuthenticationSource.IMPERSONATION,
                originalUserId = originalUserId
                ))
                ?: throw NoAccessException(302008, "Failed to create impersonation session internally", Translator.translate(ErrorStrings.COMMS_ERROR))
        val loggedIn = session.accessToken != null

        return AuthenticationResponse(
                authenticated = loggedIn,
                message = if (loggedIn) NoStrings.authGranted() else NoStrings.passwordNotVerified(),
                accessToken = session.accessToken,
            )
    }

    fun setNewPassword(userId: String, password: String?) {
        PasswordValidator.validate(password)
        val authentication = PasswordEncoder.encodePassword(password!!, EncoderType.PBKDF2)
        saveAuthentication(AuthMapper.newAuthToEntity(authentication, userId))
    }

    private fun expirePassword(authEntity: AuthenticationEntity, invalidate: Boolean = false, reason: String? = null) {
        if (!authEntity.expired) {
            authEntity.expired = true
            authEntity.expiration = Timestamp(System.currentTimeMillis())
        }
        if (invalidate && !authEntity.invalid) {
            authEntity.invalid = true
            authEntity.invalidation = Timestamp(System.currentTimeMillis())
        }
        if (reason != null && authEntity.expiredReason == null)
            authEntity.expiredReason = reason
        authRepository.save(authEntity)
    }

    private fun getUserFromLogin(loginRequest: LoginRequest, requirePassword: Boolean = true): UserEntity? {
        LoginValidator.validate(loginRequest, requirePassword)
        var userEntity = userRepository.findByEmailEquals(loginRequest.username!!)
        if (userEntity == null)
            userEntity = userRepository.findByUsernameEquals(loginRequest.username)
        if (userEntity == null)
            userEntity = userRepository.findByIdEquals(loginRequest.username)
        return userEntity
    }

    fun validateUserPassword(userEntity: UserEntity, password: String): Boolean {
        val authEntity = getAuthForUser(userEntity) ?: return false
        return PasswordEncoder.verifyPassword(UserAuthentication(
                password = password,
                hash = authEntity.hash,
                salt = authEntity.salt,
                encoder = authEntity.encoder,
                iterations = authEntity.iterations))
    }

    private fun getAuthForUser(userEntity: UserEntity): AuthenticationEntity? {
        if (userEntity.status != EntityStatus.ACTIVE) {
            logger.info("Login attempt with user ${userEntity.id} failed because this user is not active")
            if (userEntity.status == EntityStatus.INACTIVE)
                throw NoAuthException(302002, "Unable to login, user is inactive", ErrorStrings.ACTIVATE_USER)
            else if (userEntity.status == EntityStatus.DELETED)
                throw NoAuthException(302003, "Unable to login, user is deleted", ErrorStrings.DELETED_USER)
        }

        return authRepository.findTopByUserIdEqualsOrderByCreatedDesc(userEntity.id)
    }

    fun saveAuthentication(authEntity: AuthenticationEntity): AuthenticationEntity {
        return try {
            authRepository.save(authEntity)
        } catch (e: Exception) {
            logger.error("Unable to save authentication for user ${authEntity.userId}", e)
            throw NoSaveException(305002, "authentication", e)
        }
    }

    fun deleteAllUserAuthentications(userId: String) {
        try {
            authRepository.deleteAllByUserId(userId)
        } catch (e: Exception) {
            logger.error("Unable to delete authentications for user $userId", e)
            throw NoDeleteException(303002, "authentication", cause = e)
        }
    }

}
