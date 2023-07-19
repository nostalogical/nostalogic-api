package net.nostalogic.users.controllers

import io.mockk.every
import net.nostalogic.config.DatabaseLoader
import net.nostalogic.constants.AuthenticationType
import net.nostalogic.constants.NoStrings
import net.nostalogic.datamodel.ErrorResponse
import net.nostalogic.datamodel.NoDate
import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.datamodel.excomm.MessageOutline
import net.nostalogic.entities.EntitySignature
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.security.grants.ConfirmationGrant
import net.nostalogic.security.models.SessionSummary
import net.nostalogic.security.models.TokenDetails
import net.nostalogic.security.utils.TokenEncoder
import net.nostalogic.users.UsersApplication
import net.nostalogic.users.datamodel.users.*
import net.nostalogic.users.persistence.repositories.UserRepository
import net.nostalogic.utils.EntityUtils
import net.nostalogic.utils.Serialiser
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.temporal.ChronoUnit

@ActiveProfiles(profiles = ["integration-test", "test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [UsersApplication::class])
class UserControllerTest(
    @Autowired dbLoader: DatabaseLoader,
    @Autowired private val userRepository: UserRepository
): BaseControllerTest(dbLoader) {

    private val ownerId = "09acf630-1a15-49a0-bddf-cc1c0794c2f9"

    private fun <T> createUser(name: String = RandomStringUtils.random(10, true, true),
                               email: String = "${RandomStringUtils.random(5, true, true)}@nostalogic.net",
                               password: String = "Testing1.", responseType: ParameterizedTypeReference<T>): ResponseEntity<T> {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.CREATE, true)))))
        return exchange(
                entity = HttpEntity(UserRegistration(username = name, email = email, password = password)),
                responseType = responseType,
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}")
    }

    private fun <T> confirmReg(userId: String, responseType: ParameterizedTypeReference<T>): ResponseEntity<T> {
        val regCode = TokenEncoder.encodeRegistrationGrant(ConfirmationGrant(subject = userId))
        val headers = HttpHeaders()
        headers.set(NoStrings.AUTH_HEADER, regCode)
        return exchange(
                entity = HttpEntity<Unit>(headers),
                responseType = responseType,
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}${UserController.REGISTER_URI}${UserController.CONFIRM_URI}")
    }

    private fun <T> deleteUser(userId: String, responseType: ParameterizedTypeReference<T>, hard: Boolean = false): ResponseEntity<T> {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.DELETE, true),
                Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, false)))))
        val hardDelete = if (hard) "?hard=true" else ""
        return exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = responseType,
                method = HttpMethod.DELETE, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${userId}$hardDelete")
    }

    @Test
    fun `Register a user`() {
        val name = "New user name"
        val email = "newemail@nostalogic.net"
        every { excommComms.send(ofType(MessageOutline::class)) } answers { "MessageID" }
        val exchange = exchange(
                entity = HttpEntity(UserRegistration(username = name, email = email, password = "Testing1.")),
                responseType = object : ParameterizedTypeReference<RegistrationResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}${UserController.REGISTER_URI}")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        val regResponse = exchange.body as RegistrationResponse
        assertEquals(name, regResponse.displayName)
        assertEquals(email, regResponse.email)
    }

    @Test
    fun `Registration with an existing email should succeed`() {
        val name = "An available name"
        val email = "admin@nostalogic.net"
        every { excommComms.send(ofType(MessageOutline::class)) } answers { "MessageID" }
        val exchange = exchange(
            entity = HttpEntity(UserRegistration(username = name, email = email, password = "password")),
            responseType = object : ParameterizedTypeReference<RegistrationResponse>() {},
            method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}${UserController.REGISTER_URI}")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        val regResponse = exchange.body as RegistrationResponse
        assertEquals(name, regResponse.displayName)
        assertEquals(email, regResponse.email)
    }

    @Test
    fun `Register a user with a missing password`() {
        val exchange = exchange(
                entity = HttpEntity(UserRegistration(username = "New user", email = "new@nostalogic.net", password = null)),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}${UserController.REGISTER_URI}")
        assertEquals(HttpStatus.BAD_REQUEST, exchange.statusCode)
        assertEquals(307002, exchange.body!!.errorCode)
        assertTrue(exchange.body!!.userMessage.contains("password"))
    }

    @Test
    fun `Register a user with an existing name`() {
        val exchange = exchange(
                entity = HttpEntity(UserRegistration(username = "Owner", email = "admin@nostalogic.net", password = "Testing1.")),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}${UserController.REGISTER_URI}")
        assertEquals(HttpStatus.BAD_REQUEST, exchange.statusCode)
        assertTrue(exchange.body!!.userMessage.contains("username"))
        assertFalse(exchange.body!!.userMessage.contains("email"),
            "Email details should not be exposed on registration")
        assertEquals(307001, exchange.body!!.errorCode)
    }

    @Test
    fun `Create a user`() {
        val user = createUser("Generated user", "generate@nostalogic.net",
                responseType = object : ParameterizedTypeReference<User>() {}).body!!
        assertEquals("Generated user", user.username)
        assertEquals("generate@nostalogic.net", user.email)
        assertNotNull(user.id)
    }

    @Test
    fun `Create a user without permissions`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.CREATE, false)))))
        val exchange = exchange(
                entity = HttpEntity(UserRegistration(username = "name", email = "email@mail.com", password = "password")),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}")
        assertEquals(HttpStatus.FORBIDDEN, exchange.statusCode)
        assertEquals(301001, exchange.body!!.errorCode)
    }

    @Test
    fun `Create a user with an existing username and email`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.CREATE, true)))))
        val exchange = exchange(
                entity = HttpEntity(UserRegistration(username = "Owner", email = "admin@nostalogic.net", password = "password")),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}")
        assertEquals(HttpStatus.BAD_REQUEST, exchange.statusCode)
        assertEquals(307001, exchange.body!!.errorCode)
        assertTrue(exchange.body!!.userMessage.contains("username"))
        assertTrue(exchange.body!!.userMessage.contains("email"), "When directly creating a user, it " +
                "shouldn't be a problem to divulge if they already exist")
    }

    @Test
    fun `Check registration is available`() {
        val exchange = exchange(
                entity = HttpEntity(UserRegistration(username = "New user name", email = "newemail@nostalogic.net", password = null)),
                responseType = object : ParameterizedTypeReference<RegistrationAvailability>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}${UserController.REGISTER_URI}${UserController.AVAILABLE_URI}")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertTrue(exchange.body!!.usernameAvailable!!)
        assertNull(exchange.body!!.emailAvailable, "By default, email availability should not be exposed")
    }

    @Test
    fun `Email details should not be exposed when registering with an existing email`() {
        val exchange = exchange(
            entity = HttpEntity(UserRegistration(username = "An available name", email = "admin@nostalogic.net", password = null)),
            responseType = object : ParameterizedTypeReference<RegistrationAvailability>() {},
            method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}${UserController.REGISTER_URI}${UserController.AVAILABLE_URI}")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertTrue(exchange.body!!.usernameAvailable!!)
        assertNull(exchange.body!!.emailAvailable, "By default, email availability should not be exposed")
    }

    @Test
    fun `Check existing registration is unavailable`() {
        val exchange = exchange(
                entity = HttpEntity(UserRegistration(username = "Owner", email = "admin@nostalogic.net", password = null)),
                responseType = object : ParameterizedTypeReference<RegistrationAvailability>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}${UserController.REGISTER_URI}${UserController.AVAILABLE_URI}")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertFalse(exchange.body!!.usernameAvailable!!)
        assertNull(exchange.body!!.emailAvailable)
    }

    @Test
    fun `Confirm registration`() {
        val name = "Registering"
        val email = "reg@nostalogic.net"
        every { excommComms.send(ofType(MessageOutline::class)) } answers { "MessageID" }
        val exchange = exchange(
                entity = HttpEntity(UserRegistration(username = name, email = email, password = "Testing1.")),
                responseType = object : ParameterizedTypeReference<RegistrationResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}${UserController.REGISTER_URI}")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        val regResponse = exchange.body as RegistrationResponse
        assertEquals(name, regResponse.displayName)
        assertEquals(email, regResponse.email)
        val registeredUser = userRepository.findByEmailEquals(email)
        assertNotNull(registeredUser)
        val regConfirm = confirmReg(registeredUser!!.id, object : ParameterizedTypeReference<User>() {})
        assertEquals(HttpStatus.OK, regConfirm.statusCode)
        assertEquals(EntityStatus.ACTIVE, regConfirm.body!!.status)
    }

    @Test
    fun `Confirm registration with non-existent user`() {
        val regConfirm = confirmReg(EntityUtils.uuid(), object : ParameterizedTypeReference<ErrorResponse>() {})
        assertEquals(HttpStatus.NOT_FOUND, regConfirm.statusCode)
        assertEquals(304001, regConfirm.body!!.errorCode)
    }

    @Test
    fun `Confirm registration for an activate user does nothing`() {
        val regConfirm = confirmReg(ownerId, object : ParameterizedTypeReference<User>() {})
        assertEquals(HttpStatus.OK, regConfirm.statusCode)
    }

    @Test
    fun `Confirm registration on deleted user fails`() {
        deleteUser(ownerId, object : ParameterizedTypeReference<User>() {})
        val regConfirm = confirmReg(ownerId, object : ParameterizedTypeReference<ErrorResponse>() {})
        assertEquals(HttpStatus.BAD_REQUEST, regConfirm.statusCode)
        assertEquals(304002, regConfirm.body!!.errorCode)
    }

    @Test
    fun `Update a user`() {
        val name = "Change name"
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, true)))))
        val exchange = exchange(
                entity = HttpEntity(User(username = name)),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.PUT, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/$ownerId")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals(name, exchange.body!!.username)
    }

    @Test
    fun `Update a user's details`() {
        val detailsRaw = "{\"age\": 22, \"address\": {\"town\": \"Whitehaven\"}, \"hasPassport\": true, \"cities\": [\"Sheffield\", \"Oxford\"] }"
        val detailsJson = Serialiser.toJsonObject(detailsRaw)
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, true)))))
        val exchange = exchange(
            entity = HttpEntity(User(details = detailsJson)),
            responseType = object : ParameterizedTypeReference<User>() {},
            method = HttpMethod.PUT, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/$ownerId")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertNotNull(exchange.body!!.details)
        val responseJson = Serialiser.toJsonObject(exchange.body!!.details!!)
        assertEquals(detailsJson, responseJson)
        deleteUser(ownerId, object : ParameterizedTypeReference<User>() {}, true)
    }

    @Test
    fun `Update a user with no changes`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, true)))))
        val exchange = exchange(
                entity = HttpEntity(User()),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.PUT, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/$ownerId")
        assertEquals(HttpStatus.OK, exchange.statusCode)
    }

    @Test
    fun `Update a user without permission`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, false)))))
        val exchange = exchange(
                entity = HttpEntity(User(username = "Change name")),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.PUT, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/$ownerId")
        assertEquals(HttpStatus.FORBIDDEN, exchange.statusCode)
    }

    @Test
    fun `Update own password`() {
        val user = createUser(responseType = object : ParameterizedTypeReference<User>() {})
        val userId = user.body!!.id
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, false), Pair(PolicyAction.EDIT_OWN, true)))))
        val exchange = exchange(
                entity = HttpEntity(SecureUserUpdate(currentPassword = "Testing1.", password = "NewPassword.2"), testHeaders(userId)),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.PUT, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/$userId${UserController.SECURE_URI}")
        assertEquals(HttpStatus.OK, exchange.statusCode)
    }

    @Test
    fun `Update own password with invalid current password`() {
        val user = createUser(responseType = object : ParameterizedTypeReference<User>() {})
        val userId = user.body!!.id
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, false), Pair(PolicyAction.EDIT_OWN, true)))))
        val exchange = exchange(
                entity = HttpEntity(SecureUserUpdate(currentPassword = "Test1.Wrong", password = "NewPassword.2"), testHeaders(userId)),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.PUT, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/$userId${UserController.SECURE_URI}")
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.statusCode)
        assertEquals(301012, exchange.body!!.errorCode)
    }

    @Test
    fun `Update another user email`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, true)))))
        val exchange = exchange(
                entity = HttpEntity(SecureUserUpdate(email = "newemail@nostalogic.net", currentPassword = "Testing1.")),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.PUT, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/$ownerId${UserController.SECURE_URI}")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals("newemail@nostalogic.net", exchange.body!!.email)
    }

    @Test
    fun `Update user email to the existing email`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, true)))))
        val exchange = exchange(
                entity = HttpEntity(SecureUserUpdate(email = "admin@nostalogic.net", currentPassword = "Testing1.")),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.PUT, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/$ownerId${UserController.SECURE_URI}")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals("admin@nostalogic.net", exchange.body!!.email)
    }

    @Test
    fun `Update user email to an in-use email`() {
        val user = createUser(responseType = object : ParameterizedTypeReference<User>() {})
        val userId = user.body!!.id
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, true)))))
        val exchange = exchange(
                entity = HttpEntity(SecureUserUpdate(email = "admin@nostalogic.net")),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.PUT, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/$userId${UserController.SECURE_URI}")
        assertEquals(HttpStatus.BAD_REQUEST, exchange.statusCode)
        assertEquals(307007, exchange.body!!.errorCode)
    }

    @Test
    fun `Get current profile`() {
        every { accessComms.verifySession(ofType(String::class)) } answers {
            SessionSummary(
                EntityUtils.uuid(),
                ownerId,
                AuthenticationType.LOGIN,
                NoDate(),
                NoDate.plus(5L, ChronoUnit.DAYS),
                accessToken = TokenDetails("token", null)
            ) }
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders(ownerId)),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${UserController.PROFILE_URI}")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals(EntityStatus.ACTIVE, exchange.body!!.status)
        assertEquals(ownerId, exchange.body!!.id)
        assertEquals("Owner", exchange.body!!.username)
        assertEquals("admin@nostalogic.net", exchange.body!!.email)
    }

    @Test
    fun `Get current profile as guest`() {
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${UserController.PROFILE_URI}")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals("Guest", exchange.body!!.username)
        assertEquals(EntityStatus.ACTIVE, exchange.body!!.status)
        assertNull(exchange.body!!.id)
    }

    @Test
    fun `Get a user`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${ownerId}")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals("Owner", exchange.body!!.username)
        assertEquals(ownerId, exchange.body!!.id)
    }

    @Test
    fun `Get a user with specific permission`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, false), Pair(PolicyAction.EDIT, false)))),
                resourcePermissions = hashMapOf(Pair(EntitySignature(ownerId, NoEntity.USER).toString(), hashMapOf(Pair(PolicyAction.READ, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${ownerId}")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals(ownerId, exchange.body!!.id)
    }

    @Test
    fun `Get a user without permission`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, false)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${ownerId}")
        assertEquals(HttpStatus.FORBIDDEN, exchange.statusCode)
        assertEquals(301005, exchange.body!!.errorCode)
    }

    @Test
    fun `Get a non-existent user`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${EntityUtils.uuid()}")
        assertEquals(HttpStatus.NOT_FOUND, exchange.statusCode)
        assertEquals(304007, exchange.body!!.errorCode)
    }

    @Test
    fun `Delete a user`() {
        val exchange = deleteUser(ownerId, object : ParameterizedTypeReference<User>() {})
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals(EntityStatus.DELETED, exchange.body!!.status)
        val deleteConfirm = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${ownerId}")
        assertEquals(HttpStatus.OK, deleteConfirm.statusCode)
        assertEquals(EntityStatus.DELETED, deleteConfirm.body!!.status)
    }

    @Test
    fun `Hard delete a user`() {
        val exchange = deleteUser(ownerId, object : ParameterizedTypeReference<User>() {}, true)
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals(EntityStatus.DELETED, exchange.body!!.status)
        val deleteConfirm = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${ownerId}")
        assertEquals(HttpStatus.NOT_FOUND, deleteConfirm.statusCode)
    }



    @Test
    fun `Delete a user without permission`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.DELETE, false)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.DELETE, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${ownerId}")
        assertEquals(HttpStatus.FORBIDDEN, exchange.statusCode)
        assertEquals(301004, exchange.body!!.errorCode)
    }

    @Test
    fun `Get all users`() {
        for (i in 1..3)
            createUser(responseType = object : ParameterizedTypeReference<User>() {})
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, false))), Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<NoPageResponse<User>>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertTrue(exchange.body!!.size > 3)
    }

    @Test
    fun `Get users filtered by multiple fields`() {
        val users = ArrayList<User>()
        for (i in 1..6)
            users.add(createUser(responseType = object : ParameterizedTypeReference<User>() {}).body!!)
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, false))), Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<NoPageResponse<User>>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}" +
                    "?username=${users.get(0).username}&email=${users.get(2).email}&id=${users.get(5).id}")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals(3, exchange.body!!.size)
    }

    @Test
    fun `Get all users filtered by ids`() {
        val newId = createUser(responseType = object : ParameterizedTypeReference<User>() {}).body!!.id
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, false))), Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<NoPageResponse<User>>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}?id=$newId,$ownerId")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals(2, exchange.body!!.size)
    }

    @Test
    fun `Get all users with permissions for only one`() {
        createUser(responseType = object : ParameterizedTypeReference<User>() {}).body!!.id
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, false), Pair(PolicyAction.EDIT, false))), Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, true)))),
                resourcePermissions = hashMapOf(Pair(EntitySignature(ownerId, NoEntity.USER).toString(), hashMapOf(Pair(PolicyAction.READ, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<NoPageResponse<User>>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}")
        assertEquals(HttpStatus.OK, exchange.statusCode)
        assertEquals(1, exchange.body!!.size)
    }

}
