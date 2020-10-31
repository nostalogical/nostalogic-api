package net.nostalogic.users.services

import net.nostalogic.comms.Comms
import net.nostalogic.comms.ExcommComms
import net.nostalogic.config.i18n.Translator
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.ErrorStrings
import net.nostalogic.constants.MessageType
import net.nostalogic.constants.NoStrings
import net.nostalogic.crypto.encoders.EncoderType
import net.nostalogic.crypto.encoders.PasswordEncoder
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.datamodel.authentication.UserAuthentication
import net.nostalogic.datamodel.excomm.MessageOutline
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoAccessException
import net.nostalogic.exceptions.NoAuthException
import net.nostalogic.exceptions.NoRetrieveException
import net.nostalogic.exceptions.NoSaveException
import net.nostalogic.security.contexts.SessionContext
import net.nostalogic.security.grants.ImpersonationGrant
import net.nostalogic.security.grants.LoginGrant
import net.nostalogic.security.grants.PasswordResetGrant
import net.nostalogic.security.models.SessionPrompt
import net.nostalogic.security.utils.TokenEncoder
import net.nostalogic.users.constants.MembershipStatus
import net.nostalogic.users.datamodel.authentication.AuthenticationResponse
import net.nostalogic.users.datamodel.authentication.ImpersonationRequest
import net.nostalogic.users.datamodel.authentication.LoginRequest
import net.nostalogic.users.mappers.AuthMapper
import net.nostalogic.users.persistence.entities.AuthenticationEntity
import net.nostalogic.users.persistence.entities.UserEntity
import net.nostalogic.users.persistence.repositories.AuthenticationRepository
import net.nostalogic.users.persistence.repositories.MembershipRepository
import net.nostalogic.users.persistence.repositories.UserRepository
import net.nostalogic.users.validators.LoginValidator
import net.nostalogic.users.validators.PasswordValidator
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.regex.Pattern
import java.util.stream.Collectors

@Service
class UserAuthService(@Autowired private val userRepository: UserRepository,
                      @Autowired private val authRepository: AuthenticationRepository,
                      @Autowired private val membershipRepository: MembershipRepository) {

    private val logger = LoggerFactory.getLogger(UserAuthService::class.java)
    private val EMAIL_PATTERN = Pattern.compile("\\w+@\\w+\\.\\w+")

    fun login(loginRequest: LoginRequest): AuthenticationResponse {
        val userEntity = getUserFromLogin(loginRequest)
        val authEntity = getAuthForUser(userEntity)
        if (authEntity == null) {
            logger.error("No authentication entries found for user ${userEntity.id}")
            throw NoRetrieveException(304004, "Login", "No password information found for this user. Use reset password reset to resolve this problem.")
        }
        if (authEntity.invalid)
            throw NoAuthException(302004, "Account password has been invalidated. Reset is required via a password reset email.", ErrorStrings.PASSWORD_EXPIRED)
        if (authEntity.expired)
            throw NoAuthException(302005, "Account password has expired. Reset is required via secure update or password reset email.", ErrorStrings.PASSWORD_EXPIRED)

        val loggedIn = PasswordEncoder.verifyPassword(UserAuthentication(
                password = loginRequest.password!!,
                hash = authEntity.hash,
                salt = authEntity.salt,
                encoder = authEntity.encoder,
                iterations = authEntity.iterations))

        val loginType = if (EMAIL_PATTERN.matcher(loginRequest.username!!).find()) AuthenticationType.EMAIL else AuthenticationType.USERNAME
        val session = Comms.accessComms.createSession(SessionPrompt(userEntity.id, getGroupsForUser(userEntity.id), loginType))
                ?: throw NoAccessException(302006, "Failed to create session internally", Translator.translate(ErrorStrings.COMMS_ERROR))

        SessionContext.getToken()?.let { Comms.accessComms.endSession(it) }

        return AuthenticationResponse(
                authenticated = loggedIn,
                message = if (loggedIn) NoStrings.authGranted() else NoStrings.passwordNotVerified(),
                token = session.token,
                expiration = session.end)
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
            val refreshed = summary.token != null
            AuthenticationResponse(
                    authenticated = refreshed,
                    message = if (refreshed) NoStrings.authGranted() else NoStrings.authRefreshDenied(),
                    token = summary.token,
                    expiration = summary.end)
        }
    }

    fun resetPassword(loginRequest: LoginRequest): AuthenticationResponse {
        val grant = SessionContext.getGrant()
        if (grant is PasswordResetGrant)
            return createNewPasswordWithGrant(loginRequest, grant)

        val userEntity = getUserFromLogin(loginRequest, requirePassword = false)
        val authEntity = getAuthForUser(userEntity)

        return if (StringUtils.isNotBlank(loginRequest.newPassword) && (authEntity == null || authEntity.invalid))
            throw NoAuthException(301003, "No valid password exists so a new password cannot be set from the previous password",
                    Translator.translate(ErrorStrings.PASSWORD_INVALID))
        else if (StringUtils.isNotBlank(loginRequest.password) && StringUtils.isNotBlank(loginRequest.newPassword)) {
            createNewPasswordWithExpiredPassword(loginRequest, userEntity, authEntity!!)
        } else {
            sendPasswordResetEmail(userEntity)
        }
    }

    private fun createNewPasswordWithGrant(loginRequest: LoginRequest, grant: PasswordResetGrant): AuthenticationResponse {
        val userEntity = userRepository.findByIdEquals(grant.subject)
                ?: throw NoRetrieveException(304005, "User", "Supplied token is valid but no user matching subject ID ${grant.subject} exists")

        PasswordValidator.validate(loginRequest.newPassword)
        authRepository.findTopByUserIdEqualsOrderByCreatedDesc(userEntity.id)?.let { expirePassword(it, true, "A new password was set") }
        setNewPassword(userEntity.id, loginRequest.newPassword)

        val session = Comms.accessComms.createSession(SessionPrompt(userEntity.id, getGroupsForUser(userEntity.id), AuthenticationType.PASSWORD_RESET, reset = true))
                ?: throw NoAccessException(302011, "Failed to create session internally", Translator.translate(ErrorStrings.COMMS_ERROR))
        return AuthenticationResponse(
                authenticated = session.token != null,
                message = if (session.token != null) NoStrings.passwordChanged() else NoStrings.sessionCreateFail(),
                token = session.token,
                expiration = session.end)
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

            val loginType = if (EMAIL_PATTERN.matcher(loginRequest.username!!).find()) AuthenticationType.EMAIL else AuthenticationType.USERNAME
            val session = Comms.accessComms.createSession(SessionPrompt(userEntity.id, getGroupsForUser(userEntity.id), loginType, reset = true))
                    ?: throw NoAccessException(302011, "Failed to create session internally", Translator.translate(ErrorStrings.COMMS_ERROR))

            return AuthenticationResponse(
                    authenticated = session.token != null,
                    message = if (session.token != null) NoStrings.passwordChanged() else NoStrings.sessionCreateFail(),
                    token = session.token,
                    expiration = session.end)
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
        if (grant is ImpersonationGrant) {
            alternates.addAll(grant.alternateSubjects)
            alternates.add(grant.subject)
        }

        if (impRequest.userId == originalUserId)
            throw NoAccessException(302009, "A user cannot impersonate themselves", NoStrings.impersonationDenied())
        if (!AccessQuery().simpleCheck(id = impRequest.userId, entity = NoEntity.USER, action = PolicyAction.EDIT))
            throw NoAccessException(302010, "Missing edit permission for this user, which is required for impersonation", NoStrings.impersonationDenied())

        val session = Comms.accessComms.createSession(SessionPrompt(
                userId = impRequest.userId,
                additional = getGroupsForUser(impRequest.userId),
                type = AuthenticationType.IMPERSONATION,
                originalUserId = originalUserId,
                alternates = alternates))
                ?: throw NoAccessException(302008, "Failed to create impersonation session internally", Translator.translate(ErrorStrings.COMMS_ERROR))
        val loggedIn = session.token != null

        return AuthenticationResponse(
                authenticated = loggedIn,
                message = if (loggedIn) NoStrings.authGranted() else NoStrings.passwordNotVerified(),
                token = session.token,
                expiration = session.end)
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

    private fun getUserFromLogin(loginRequest: LoginRequest, requirePassword: Boolean = true): UserEntity {
        LoginValidator.validate(loginRequest, requirePassword)
        var userEntity = userRepository.findByEmailEquals(loginRequest.username!!)
        if (userEntity == null)
            userEntity = userRepository.findByNameEquals(loginRequest.username)
        if (userEntity == null)
            userEntity = userRepository.findByIdEquals(loginRequest.username)
        if (userEntity == null)
            throw NoRetrieveException(304003, "User", "Supplied username does not match any registered username or email")
        return userEntity
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

    private fun getGroupsForUser(userId: String): Set<String> {
        val groups = membershipRepository.findAllByUserIdEqualsAndStatusIn(userId, setOf(MembershipStatus.ACTIVE))
        return groups.stream().map { it.groupId }.collect(Collectors.toSet())
    }

    fun saveAuthentication(authEntity: AuthenticationEntity): AuthenticationEntity {
        return try {
            authRepository.save(authEntity)
        } catch (e: Exception) {
            logger.error("Unable to save authentication for user ${authEntity.userId}", e)
            throw NoSaveException(305002, "authentication", e)
        }
    }

}
