package net.nostalogic.users.services

import net.nostalogic.comms.Comms
import net.nostalogic.config.Config
import net.nostalogic.config.i18n.Translator
import net.nostalogic.constants.*
import net.nostalogic.constants.ExceptionCodes._0305001
import net.nostalogic.crypto.encoders.EncoderType
import net.nostalogic.crypto.encoders.PasswordEncoder
import net.nostalogic.datamodel.NoPageable
import net.nostalogic.datamodel.access.*
import net.nostalogic.datamodel.excomm.MessageOutline
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.*
import net.nostalogic.security.contexts.SessionContext
import net.nostalogic.security.grants.ConfirmationGrant
import net.nostalogic.security.utils.TokenDecoder
import net.nostalogic.security.utils.TokenEncoder
import net.nostalogic.users.datamodel.memberships.Membership
import net.nostalogic.users.datamodel.memberships.MembershipSearchCriteria
import net.nostalogic.users.datamodel.users.*
import net.nostalogic.users.mappers.AuthMapper
import net.nostalogic.users.mappers.UserMapper
import net.nostalogic.users.persistence.entities.UserEntity
import net.nostalogic.users.persistence.repositories.UserRepository
import net.nostalogic.users.validators.PasswordValidator
import net.nostalogic.users.validators.RegistrationValidator
import net.nostalogic.users.validators.UserValidator
import net.nostalogic.utils.Serialiser
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class UserService(
        @Autowired private val userRepository: UserRepository,
        @Autowired private val membershipService: MembershipService,
        @Autowired private val authService: UserAuthService,
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun createUser(userRegistration: UserRegistration): User {
        if (!AccessQuery().simpleCheck(entity = NoEntity.USER, action = PolicyAction.CREATE))
            throw NoAccessException(301001, "Current user does not have create permissions for users")

        val registrationCheck = checkRegistrationAvailable(userRegistration)
        RegistrationValidator.validateRegistration(userRegistration, registrationCheck)
        PasswordValidator.simpleValidate(userRegistration.password)

        val authentication = PasswordEncoder.encodePassword(userRegistration.password!!, EncoderType.PBKDF2)
        val userEntity = saveUser(UserMapper.registrationToEntity(
            userRegistration = userRegistration,
            status = EntityStatus.ACTIVE,
            tag = generateUsernameTag(userRegistration.username!!, registrationCheck),
        ))
        authService.saveAuthentication(AuthMapper.tempAuthToEntity(authentication, userEntity.id, "Temporary password for manually created user"))

        return UserMapper.entityToDto(userEntity)
    }

    fun registerUser(userRegistration: UserRegistration): RegistrationResponse {
        val registrationCheck = checkRegistrationAvailable(userRegistration)

        RegistrationValidator.validateRegistration(userRegistration, registrationCheck, requireAvailableEmail = false)
        if (registrationCheck.emailAvailable == false)
            return sendRegistrationPasswordResetEmail(userRegistration)
        return registerNewUser(userRegistration, registrationCheck)
    }

    /**
     * If registration is done with an existing email, trigger a customised password reset email to be sent to it, and
     * then return a generic "registered" response to the user. This prevents leaking information on registered emails.
     */
    private fun sendRegistrationPasswordResetEmail(userRegistration: UserRegistration): RegistrationResponse {
        val userEntity = userRepository.findByEmailEquals(userRegistration.email!!)!!
        val regCode = TokenEncoder.encodeRegistrationGrant(ConfirmationGrant(subject = userEntity.id))
        Comms.excomm().send(MessageOutline(
            recipientId = userEntity.id,
            recipientEmailAddress = userEntity.email,
            type = MessageType.REGISTRATION_RESET,
            locale = userEntity.locale)
            .setParameter("reg_code", regCode))
        return RegistrationResponse(userRegistration.email, userRegistration.username!!)
    }

    private fun registerNewUser(userRegistration: UserRegistration, availability: RegistrationAvailability): RegistrationResponse {
        val userEntity = saveUser(UserMapper.registrationToEntity(
            userRegistration = userRegistration,
            tag = generateUsernameTag(userRegistration.username!!, availability),
        ))
        authService.setNewPassword(userEntity.id, userRegistration.password)

        val regCode = TokenEncoder.encodeRegistrationGrant(ConfirmationGrant(subject = userEntity.id))
        Comms.excomm().send(MessageOutline(
            recipientId = userEntity.id,
            recipientEmailAddress = userEntity.email,
            type = MessageType.REGISTRATION_CONFIRM,
            locale = userEntity.locale)
            .setParameter("reg_code", regCode))
        return RegistrationResponse(userEntity.email, userRegistration.username)
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

    fun checkRegistrationAvailable(userRegistration: UserRegistration, checkEmail: Boolean = true): RegistrationAvailability {
        val rule = Config.usernameRule(SessionContext.getTenant())

        val nameAvailable =
            if (rule == UsernameRule.ALWAYS_TAG) true
            else userRegistration.username?.let { userRepository.isUsernameAvailable(it) }
        val taggingRequired = rule == UsernameRule.ALWAYS_TAG || (nameAvailable == false && rule == UsernameRule.AUTO_TAG)
        val emailAvailable = if (!checkEmail) null else userRegistration.email?.let { userRepository.isEmailAvailable(it) }

        return RegistrationAvailability(
            usernameAvailable = nameAvailable,
            taggingRequired = taggingRequired,
            emailAvailable = emailAvailable,
        )
    }

    private fun saveUser(userEntity: UserEntity): UserEntity {
        return try {
            userRepository.save(userEntity)
        } catch (e: Exception) {
            logger.error("Unable to save user ${userEntity.id}", e)
            throw NoSaveException(_0305001, "user", e)
        }
    }

    private fun hardDeleteUser(userEntity: UserEntity): UserEntity {
        return try {
            authService.deleteAllUserAuthentications(userEntity.id)
            membershipService.deleteUserFromAllGroups(userEntity.id)
            userRepository.delete(userEntity)
            userEntity
        } catch (e: Exception) {
            logger.error("Unable to delete user ${userEntity.id}", e)
            throw NoDeleteException(code = 303001, objectName = "user", cause = e)
        }
    }

    fun deleteUser(userId: String, hard: Boolean = false): User {
        if (!AccessQuery().simpleCheck(userId, NoEntity.USER, PolicyAction.DELETE))
            throw NoAccessException(301004, "You do not have delete permission for user $userId")
        val userEntities = userRepository.updateUsersStatus(setOf(userId), EntityStatus.DELETED.name)
        if (userEntities.isEmpty())
            throw NoRetrieveException(304006, "User")
        val deletedUser = userEntities.first()
        if (hard)
            hardDeleteUser(deletedUser)
        return UserMapper.entityToDto(deletedUser)
    }

    fun getUser(userId: String): User {
        val query = AccessQuery().currentSubject()
            .addQuery(null, NoEntity.USER, PolicyAction.READ)
            .addQuery(userId, NoEntity.USER, PolicyAction.EDIT)
        if (SessionContext.getUserId() == userId)
            query.addQuery(null, NoEntity.USER, PolicyAction.EDIT_OWN)
        val report = query.toReport()
        if (!report.hasPermission(EntityReference(userId, NoEntity.USER), PolicyAction.READ))
            throw NoAccessException(301005, "You do not have read permission for user $userId")
        val userEntity = getUserEntity(userId)
        return UserMapper.entityToDto(userEntity, includeSensitive = hasUserEditPermissions(report, userId))
    }

    private fun getUserEntity(userId: String): UserEntity {
        return userRepository.findByIdEquals(userId) ?: throw NoRetrieveException(304007, "User")
    }

    fun getCurrentUser(includeMemberships: Boolean, includeRights: Boolean): User {
        return if (!SessionContext.isLoggedIn())
            User(username = NoStrings.guest())
        else {
            val userId = SessionContext.getUserId()
            val userEntity = userRepository.findByIdEquals(userId) ?: throw NoRetrieveException(304012, "User")

            val rights = if (!includeRights) null else {
                val query = AccessQuery().currentSubject()
                for (entity in setOf(NoEntity.USER, NoEntity.GROUP, NoEntity.NAV, NoEntity.ARTICLE))
                    query.addQuery(entity, PolicyAction.READ, PolicyAction.CREATE, PolicyAction.EDIT)
                val report = query.toReport()
                report.entityPermissions.map { it.key to EntityRights(
                    read = it.value[PolicyAction.READ],
                    edit = it.value[PolicyAction.EDIT],
                    create = it.value[PolicyAction.CREATE]) }.toMap()
            }

            val memberships = if (!includeMemberships) null else {
                val pageable = NoPageable<Membership>(sortFields = *UserSearchCriteria.DEFAULT_SORT_FIELDS)
                val criteria = MembershipSearchCriteria(userIds = setOf(userId), page = pageable, rights = includeRights)
                pageable.toResponse(membershipService.getMemberships(searchCriteria = criteria, showUsers = false))
            }
            UserMapper.entityToDto(userEntity, memberships = memberships, rights = rights)
        }
    }

    fun getUsers(searchCriteria: UserSearchCriteria): List<User> {
        val query = AccessQuery().currentSubject()
            .addQuery(null, NoEntity.USER, PolicyAction.READ)
            .addQuery(null, NoEntity.USER, PolicyAction.EDIT)
            .addQuery(null, NoEntity.USER, PolicyAction.EDIT_OWN)
            .addQuery(null, NoEntity.GROUP, PolicyAction.READ)
        if (searchCriteria.userIds.isNotEmpty()) {
            query.addQuery(searchCriteria.userIds, NoEntity.USER, PolicyAction.READ)
            query.addQuery(searchCriteria.userIds, NoEntity.USER, PolicyAction.EDIT)
        }
        if (searchCriteria.memberGroupIds.isNotEmpty())
            query.addQuery(searchCriteria.memberGroupIds, NoEntity.GROUP, PolicyAction.READ)
        val report = query.toReport()

        val userIds: Set<String>? = if (searchCriteria.userIds.isEmpty() && report.hasPermission(EntityReference(entity = NoEntity.USER), PolicyAction.READ)) null
            else if (searchCriteria.userIds.isEmpty()) report.permittedForEntity(NoEntity.USER, PolicyAction.READ)
            else report.filterByPermitted(searchCriteria.userIds, NoEntity.USER, PolicyAction.READ)
        val groupIds: Set<String>? =
            if (searchCriteria.memberGroupIds.isEmpty() && report.hasPermission(EntityReference(entity = NoEntity.GROUP), PolicyAction.READ)) null
            else if (searchCriteria.memberGroupIds.isEmpty()) report.permittedForEntity(NoEntity.GROUP, PolicyAction.READ)
            else report.filterByPermitted(searchCriteria.memberGroupIds, NoEntity.GROUP, PolicyAction.READ)

        val status = searchCriteria.status.map { it.name }.toSet()
        val usernames = searchCriteria.usernames.toSet()
        val emails = searchCriteria.emails.toSet()
        val page = searchCriteria.page.toQuery()

        val userEntities =
            when {
                userIds == null && groupIds == null && emails.isEmpty() && usernames.isEmpty() -> userRepository.searchUsers(status, page)
                userIds == null && groupIds?.isNotEmpty() == true && emails.isEmpty() && usernames.isEmpty() -> userRepository.searchUsersByGroups(groupIds, status, page)
                groupIds == null -> userRepository.searchUsersByIdentifiers(userIds?: emptySet(), usernames, emails, status, page)
                else -> userRepository.searchUsersByIdentifiersAndGroups(userIds?: emptySet(), groupIds, usernames, emails, status, page)
            }
        searchCriteria.page.setResponseMetadata(userEntities)

        return userEntities.map { UserMapper.entityToDto(it, includeSensitive = hasUserEditPermissions(report, it.id)) }.toList()
    }

    private fun hasUserEditPermissions(report: AccessReport, userId: String): Boolean {
        val allowed = report.hasPermission(EntityReference(userId, NoEntity.USER), PolicyAction.EDIT)
        return if (!allowed && userId == SessionContext.getUserId())
            report.hasPermission(EntityReference(userId, NoEntity.USER), PolicyAction.EDIT_OWN)
        else allowed
    }

    fun updateUser(userId: String, update: User): User {
        if (!SimpleAccess(userId, NoEntity.USER, PolicyAction.EDIT).check())
            throw NoAccessException(301006, "You do not have edit permission for user $userId")

        val userEntity = userRepository.findByIdEquals(userId) ?: throw NoRetrieveException(304008, "User")
        UserValidator.validateUpdate(update, userId,
                update.username?.let { userRepository.findByUsernameEquals(it) })

        if (StringUtils.isNotBlank(update.username))
            userEntity.username = update.username!!
        if (StringUtils.isNotBlank(update.locale))
            userEntity.locale = NoLocale.fromString(update.locale!!)!!
        detailsToJson(update.details)?.let { userEntity.details = it }

        return UserMapper.entityToDto(saveUser(userEntity))
    }

    fun secureUpdate(userId: String, update: SecureUserUpdate): User {
        if (!AccessQuery().simpleCheck(userId, NoEntity.USER, PolicyAction.EDIT))
            throw NoAccessException(301011, "You do not have edit permission for user $userId")

        val userEntity = getUserEntity(userId)
        UserValidator.validateSecureUpdate(userId, update, update.email?.let { userRepository.findByEmailEquals(it) })

        val selfChange = SessionContext.getUserId() == userId
        if (selfChange && !authService.validateUserPassword(userEntity, update.currentPassword!!))
            throw NoAuthException(301012,
                    debugMessage = "Password failed verification for user $userId, unable to perform secure update",
                    userMessage = Translator.translate(ErrorStrings.WRONG_PASSWORD_UPDATE))

        if (StringUtils.isNotBlank(update.password)) {
            PasswordValidator.simpleValidate(update.password)
            val authentication = PasswordEncoder.encodePassword(update.password!!, EncoderType.PBKDF2)
            if (selfChange)
                authService.saveAuthentication(AuthMapper.newAuthToEntity(authentication, userId))
            else
                authService.saveAuthentication(AuthMapper.tempAuthToEntity(authentication, userId,
                    "Temporary password from an update by another user"))
        }
        if (StringUtils.isNotBlank(update.email)) {
            userEntity.email = update.email!!
            saveUser(userEntity)
        }

        return UserMapper.entityToDto(userEntity)
    }

    private fun generateUsernameTag(baseUsername: String, availability: RegistrationAvailability): String? {
        if (!availability.taggingRequired)
            return null

        var tag: String
        var fullUsername: String
        do {
            tag = RandomStringUtils.random(6, false, true)
            fullUsername = UserMapper.usernameWithTag(baseUsername, tag)
        } while (userRepository.findByUsernameEquals(fullUsername) != null)
        return tag
    }

    private fun detailsToJson(details: Any?): String? {
        return try {
            details?.let { Serialiser.serialise(details) }
        } catch (e: Exception) {
            return null
        }
    }

    fun checkRights(): EntityRights {
        val userEntity = EntityReference(entity = NoEntity.USER)
        val report = AccessQuery().currentSubject()
            .addQuery(userEntity, PolicyAction.CREATE, PolicyAction.DELETE, PolicyAction.EDIT)
            .toReport()
        return EntityRights(
            create = report.hasPermission(userEntity, PolicyAction.CREATE),
            edit = report.hasPermission(userEntity, PolicyAction.EDIT),
            delete = report.hasPermission(userEntity, PolicyAction.DELETE))
    }

}
