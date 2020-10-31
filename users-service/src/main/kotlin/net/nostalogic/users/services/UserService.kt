package net.nostalogic.users.services

import net.nostalogic.comms.ExcommComms
import net.nostalogic.config.i18n.Translator
import net.nostalogic.constants.MessageType
import net.nostalogic.crypto.encoders.EncoderType
import net.nostalogic.crypto.encoders.PasswordEncoder
import net.nostalogic.datamodel.ChangeSummary
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.AccessReport
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.datamodel.excomm.MessageOutline
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoAccessException
import net.nostalogic.exceptions.NoAuthException
import net.nostalogic.exceptions.NoRetrieveException
import net.nostalogic.exceptions.NoSaveException
import net.nostalogic.security.contexts.SessionContext
import net.nostalogic.security.grants.ConfirmationGrant
import net.nostalogic.security.utils.TokenDecoder
import net.nostalogic.security.utils.TokenEncoder
import net.nostalogic.users.datamodel.RegistrationAvailability
import net.nostalogic.users.datamodel.User
import net.nostalogic.users.datamodel.UserRegistration
import net.nostalogic.users.mappers.AuthMapper
import net.nostalogic.users.mappers.UserMapper
import net.nostalogic.users.persistence.entities.UserEntity
import net.nostalogic.users.persistence.repositories.UserRepository
import net.nostalogic.users.validators.PasswordValidator
import net.nostalogic.users.validators.RegistrationValidator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class UserService(
        @Autowired private val userRepository: UserRepository,
        @Autowired private val authService: UserAuthService) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun createUser(userRegistration: UserRegistration): User {
        if (!AccessQuery().simpleCheck(entity = NoEntity.USER, action = PolicyAction.CREATE))
            throw NoAccessException(301001, "Current user does not have create permissions for users")

        RegistrationValidator.validateRegistration(userRegistration, checkRegistrationAvailable(userRegistration))
        PasswordValidator.simpleValidate(userRegistration.password)

        val authentication = PasswordEncoder.encodePassword(userRegistration.password!!, EncoderType.PBKDF2)
        val userEntity = saveUser(UserMapper.registrationToEntity(userRegistration = userRegistration, status = EntityStatus.ACTIVE))
        authService.saveAuthentication(AuthMapper.tempAuthToEntity(authentication, userEntity.id, "Temporary password for manually created user"))

        return UserMapper.entityToDto(userEntity)
    }

    fun registerUser(userRegistration: UserRegistration): User {
        RegistrationValidator.validateRegistration(userRegistration, checkRegistrationAvailable(userRegistration))

        val userEntity = saveUser(UserMapper.registrationToEntity(userRegistration = userRegistration))

//        PasswordValidator.validate(userRegistration.password)
//        val authentication = PasswordEncoder.encodePassword(userRegistration.password!!, EncoderType.PBKDF2)
//        authService.saveAuthentication(AuthMapper.newAuthToEntity(authentication, userEntity.id))
        authService.setNewPassword(userEntity.id, userRegistration.password)

        val regCode = TokenEncoder.encodeRegistrationGrant(ConfirmationGrant(subject = userEntity.id))
        ExcommComms.send(MessageOutline(
                recipientId = userEntity.id,
                recipientEmailAddress = userEntity.email,
                type = MessageType.REGISTRATION_CONFIRM,
                locale = userEntity.locale)
                .setParameter("reg_code", regCode))
        return UserMapper.entityToDto(userEntity)

    }

    fun confirmRegistration(token: String?): User {
        val grant = TokenDecoder.decodeToken(token.orEmpty())
        if (grant is ConfirmationGrant) {
            val user = userRepository.findByIdOrNull(grant.subject)
                    ?: throw NoRetrieveException(304001, "User", "Confirmation token is valid but no user was found with ID ${grant.subject}")
            return when (user.status) {
                EntityStatus.ACTIVE -> {
                    logger.info("Ignoring activation attempt for already active user ${user.id}")
                    UserMapper.entityToDto(user)
                }
                EntityStatus.INACTIVE -> {
                    user.status = EntityStatus.ACTIVE
                    UserMapper.entityToDto(saveUser(user))
                }
                else -> throw NoSaveException(code = 304002, objectName = "user",
                        debugMessage = "User cannot be activated from status ${user.status}", status = HttpStatus.BAD_REQUEST)
            }
        } else
            throw NoAuthException(302001, "Token is not a registration confirmation token", Translator.translate("authMethodMismatch"))
    }

    fun checkRegistrationAvailable(userRegistration: UserRegistration): RegistrationAvailability {
        return RegistrationAvailability(
                usernameAvailable = userRegistration.username?.let { userRepository.isUsernameAvailable(it) },
                emailAvailable = userRegistration.email?.let { userRepository.isEmailAvailable(it) })
    }

//    fun secureUpdateUser() {
//
//    }

    private fun changeUsersStatus(ids: Collection<String>, status: EntityStatus, accessReport: AccessReport? = null): Set<ChangeSummary> {
        val currentUser = SessionContext.getUserId()
        val rights = accessReport ?: AccessQuery().currentSubject()
                .addQuery(ids, NoEntity.USER, PolicyAction.EDIT)
                .addQuery(currentUser, NoEntity.USER, PolicyAction.EDIT_OWN).toReport()
        val changes = HashSet<ChangeSummary>()
        val idsToUpdate = HashSet<String>()
        for (id in ids) {
            val canChange = id == currentUser && rights.hasPermission(EntityReference(id, NoEntity.USER), PolicyAction.EDIT_OWN)
                    || rights.hasPermission(EntityReference(id, NoEntity.USER), PolicyAction.EDIT)
            changes.add(ChangeSummary(canChange, id, NoEntity.USER))
            if (canChange)
                idsToUpdate.add(id)
        }
        userRepository.updateUsersStatus(idsToUpdate, status)
        return changes
    }

    private fun saveUser(userEntity: UserEntity): UserEntity {
        return try {
            userRepository.save(userEntity)
        } catch (e: Exception) {
            logger.error("Unable to save user ${userEntity.id}", e)
            throw NoSaveException(305001, "user", e)
        }
    }

    fun updateUserStatus() {

    }

    fun deleteUsers() {

    }
}
