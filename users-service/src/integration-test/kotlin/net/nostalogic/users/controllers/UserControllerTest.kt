package net.nostalogic.users.controllers

import net.nostalogic.config.DatabaseLoader
import net.nostalogic.constants.NoStrings
import net.nostalogic.datamodel.ErrorResponse
import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntitySignature
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.security.grants.ConfirmationGrant
import net.nostalogic.security.utils.TokenEncoder
import net.nostalogic.users.UsersApplication
import net.nostalogic.users.datamodel.users.RegistrationAvailability
import net.nostalogic.users.datamodel.users.SecureUserUpdate
import net.nostalogic.users.datamodel.users.User
import net.nostalogic.users.datamodel.users.UserRegistration
import net.nostalogic.utils.EntityUtils
import org.apache.commons.lang3.RandomStringUtils
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@Suppress("FunctionName")
@ActiveProfiles(profiles = ["integration-test"])
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [UsersApplication::class])
class UserControllerTest(@Autowired dbLoader: DatabaseLoader): BaseControllerTest(dbLoader) {

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
        val exchange = exchange(
                entity = HttpEntity(UserRegistration(username = name, email = email, password = "Testing1.")),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}${UserController.REGISTER_URI}")
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertEquals(name, exchange.body!!.username)
        Assertions.assertEquals(email, exchange.body!!.email)
        Assertions.assertEquals(EntityStatus.INACTIVE, exchange.body!!.status)
    }

    @Test
    fun `Register a user with a missing password`() {
        val exchange = exchange(
                entity = HttpEntity(UserRegistration(username = "New user", email = "new@nostalogic.net", password = null)),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}${UserController.REGISTER_URI}")
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, exchange.statusCode)
        Assertions.assertEquals(307002, exchange.body!!.errorCode)
        Assertions.assertTrue(exchange.body!!.userMessage.contains("password"))
    }

    @Test
    fun `Register a user with an existing name`() {
        val exchange = exchange(
                entity = HttpEntity(UserRegistration(username = "Owner", email = "admin@nostalogic.net", password = "Testing1.")),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}${UserController.REGISTER_URI}")
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, exchange.statusCode)
        Assertions.assertTrue(exchange.body!!.userMessage.contains("username"))
        Assertions.assertTrue(exchange.body!!.userMessage.contains("email"))
        Assertions.assertEquals(307001, exchange.body!!.errorCode)
    }

    @Test
    fun `Create a user`() {
        val user = createUser("Generated user", "generate@nostalogic.net",
                responseType = object : ParameterizedTypeReference<User>() {}).body!!
        Assertions.assertEquals("Generated user", user.username)
        Assertions.assertEquals("generate@nostalogic.net", user.email)
        Assertions.assertNotNull(user.id)
    }

    @Test
    fun `Create a user without permissions`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.CREATE, false)))))
        val exchange = exchange(
                entity = HttpEntity(UserRegistration(username = "name", email = "email@mail.com", password = "password")),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}")
        Assertions.assertEquals(HttpStatus.FORBIDDEN, exchange.statusCode)
        Assertions.assertEquals(301001, exchange.body!!.errorCode)
    }

    @Test
    fun `Create a user with an existing username and email`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.CREATE, true)))))
        val exchange = exchange(
                entity = HttpEntity(UserRegistration(username = "Owner", email = "admin@nostalogic.net", password = "password")),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}")
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, exchange.statusCode)
        Assertions.assertEquals(307001, exchange.body!!.errorCode)
        Assertions.assertTrue(exchange.body!!.userMessage.contains("username"))
        Assertions.assertTrue(exchange.body!!.userMessage.contains("email"))
    }

    @Test
    fun `Check registration is available`() {
        val exchange = exchange(
                entity = HttpEntity(UserRegistration(username = "New user name", email = "newemail@nostalogic.net", password = null)),
                responseType = object : ParameterizedTypeReference<RegistrationAvailability>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}${UserController.REGISTER_URI}${UserController.AVAILABLE_URI}")
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertTrue(exchange.body!!.usernameAvailable!!)
        Assertions.assertTrue(exchange.body!!.emailAvailable!!)
    }

    @Test
    fun `Check existing registration is unavailable`() {
        val exchange = exchange(
                entity = HttpEntity(UserRegistration(username = "Owner", email = "admin@nostalogic.net", password = null)),
                responseType = object : ParameterizedTypeReference<RegistrationAvailability>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}${UserController.REGISTER_URI}${UserController.AVAILABLE_URI}")
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertFalse(exchange.body!!.usernameAvailable!!)
        Assertions.assertFalse(exchange.body!!.emailAvailable!!)
    }

    @Test
    fun `Confirm registration`() {
        val name = "Registering"
        val email = "reg@nostalogic.net"
        val exchange = exchange(
                entity = HttpEntity(UserRegistration(username = name, email = email, password = "Testing1.")),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.POST, url = "$baseApiUrl${UserController.USERS_ENDPOINT}${UserController.REGISTER_URI}")
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertNotNull(exchange.body!!.id)
        val regConfirm = confirmReg(exchange.body!!.id!!, object : ParameterizedTypeReference<User>() {})
        Assertions.assertEquals(HttpStatus.OK, regConfirm.statusCode)
        Assertions.assertEquals(EntityStatus.ACTIVE, regConfirm.body!!.status)
    }

    @Test
    fun `Confirm registration with non-existent user`() {
        val regConfirm = confirmReg(EntityUtils.uuid(), object : ParameterizedTypeReference<ErrorResponse>() {})
        Assertions.assertEquals(HttpStatus.NOT_FOUND, regConfirm.statusCode)
        Assertions.assertEquals(304001, regConfirm.body!!.errorCode)
    }

    @Test
    fun `Confirm registration for an activate user does nothing`() {
        val regConfirm = confirmReg(ownerId, object : ParameterizedTypeReference<User>() {})
        Assertions.assertEquals(HttpStatus.OK, regConfirm.statusCode)
    }

    @Test
    fun `Confirm registration on deleted user fails`() {
        deleteUser(ownerId, object : ParameterizedTypeReference<User>() {})
        val regConfirm = confirmReg(ownerId, object : ParameterizedTypeReference<ErrorResponse>() {})
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, regConfirm.statusCode)
        Assertions.assertEquals(304002, regConfirm.body!!.errorCode)
    }

    @Test
    fun `Update a user`() {
        val name = "Change name"
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, true)))))
        val exchange = exchange(
                entity = HttpEntity(User(username = name)),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.PUT, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/$ownerId")
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertEquals(name, exchange.body!!.username)
    }

    @Test
    fun `Update a user's details`() {
        val detailsRaw = "{\"age\": 22, \"address\": {\"town\": \"Whitehaven\"}, \"hasPassport\": true, \"cities\": [\"Sheffield\", \"Oxford\"] }"
        val detailsInput = JSONObject(detailsRaw)
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, true)))))
        val exchange = exchange(
            entity = HttpEntity(User(details = detailsRaw)),
            responseType = object : ParameterizedTypeReference<User>() {},
            method = HttpMethod.PUT, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/$ownerId")
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertNotNull(exchange.body!!.details)
        Assertions.assertEquals(detailsInput.toString(), exchange.body!!.details)
        deleteUser(ownerId, object : ParameterizedTypeReference<User>() {}, true)
    }

    @Test
    fun `Update a user with no changes`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, true)))))
        val exchange = exchange(
                entity = HttpEntity(User()),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.PUT, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/$ownerId")
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
    }

    @Test
    fun `Update a user without permission`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, false)))))
        val exchange = exchange(
                entity = HttpEntity(User(username = "Change name")),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.PUT, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/$ownerId")
        Assertions.assertEquals(HttpStatus.FORBIDDEN, exchange.statusCode)
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
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
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
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exchange.statusCode)
        Assertions.assertEquals(301012, exchange.body!!.errorCode)
    }

    @Test
    fun `Update another user email`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, true)))))
        val exchange = exchange(
                entity = HttpEntity(SecureUserUpdate(email = "newemail@nostalogic.net", currentPassword = "Testing1.")),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.PUT, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/$ownerId${UserController.SECURE_URI}")
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertEquals("newemail@nostalogic.net", exchange.body!!.email)
    }

    @Test
    fun `Update user email to the existing email`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, true)))))
        val exchange = exchange(
                entity = HttpEntity(SecureUserUpdate(email = "admin@nostalogic.net", currentPassword = "Testing1.")),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.PUT, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/$ownerId${UserController.SECURE_URI}")
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertEquals("admin@nostalogic.net", exchange.body!!.email)
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
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, exchange.statusCode)
        Assertions.assertEquals(307007, exchange.body!!.errorCode)
    }

    @Test
    fun `Get current profile`() {
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders(ownerId)),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${UserController.PROFILE_URI}")
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertEquals(EntityStatus.ACTIVE, exchange.body!!.status)
        Assertions.assertEquals(ownerId, exchange.body!!.id)
        Assertions.assertEquals("Owner", exchange.body!!.username)
        Assertions.assertEquals("admin@nostalogic.net", exchange.body!!.email)
    }

    @Test
    fun `Get current profile as guest`() {
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${UserController.PROFILE_URI}")
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertEquals("Guest", exchange.body!!.username)
        Assertions.assertEquals(EntityStatus.ACTIVE, exchange.body!!.status)
        Assertions.assertNull(exchange.body!!.id)
    }

    @Test
    fun `Get a user`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${ownerId}")
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertEquals("Owner", exchange.body!!.username)
        Assertions.assertEquals(ownerId, exchange.body!!.id)
    }

    @Test
    fun `Get a user with specific permission`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, false), Pair(PolicyAction.EDIT, false)))),
                resourcePermissions = hashMapOf(Pair(EntitySignature(ownerId, NoEntity.USER).toString(), hashMapOf(Pair(PolicyAction.READ, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${ownerId}")
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertEquals(ownerId, exchange.body!!.id)
    }

    @Test
    fun `Get a user without permission`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, false)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${ownerId}")
        Assertions.assertEquals(HttpStatus.FORBIDDEN, exchange.statusCode)
        Assertions.assertEquals(301005, exchange.body!!.errorCode)
    }

    @Test
    fun `Get a non-existent user`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${EntityUtils.uuid()}")
        Assertions.assertEquals(HttpStatus.NOT_FOUND, exchange.statusCode)
        Assertions.assertEquals(304007, exchange.body!!.errorCode)
    }

    @Test
    fun `Delete a user`() {
        val exchange = deleteUser(ownerId, object : ParameterizedTypeReference<User>() {})
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertEquals(EntityStatus.DELETED, exchange.body!!.status)
        val deleteConfirm = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<User>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${ownerId}")
        Assertions.assertEquals(HttpStatus.OK, deleteConfirm.statusCode)
        Assertions.assertEquals(EntityStatus.DELETED, deleteConfirm.body!!.status)
    }

    @Test
    fun `Hard delete a user`() {
        val exchange = deleteUser(ownerId, object : ParameterizedTypeReference<User>() {}, true)
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertEquals(EntityStatus.DELETED, exchange.body!!.status)
        val deleteConfirm = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${ownerId}")
        Assertions.assertEquals(HttpStatus.NOT_FOUND, deleteConfirm.statusCode)
    }



    @Test
    fun `Delete a user without permission`() {
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.DELETE, false)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<ErrorResponse>() {},
                method = HttpMethod.DELETE, url = "$baseApiUrl${UserController.USERS_ENDPOINT}/${ownerId}")
        Assertions.assertEquals(HttpStatus.FORBIDDEN, exchange.statusCode)
        Assertions.assertEquals(301004, exchange.body!!.errorCode)
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
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertTrue(exchange.body!!.size > 3)
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
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertEquals(3, exchange.body!!.size)
    }

    @Test
    fun `Get all users filtered by ids`() {
        val newId = createUser(responseType = object : ParameterizedTypeReference<User>() {}).body!!.id
        mockPermissions(entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.READ, true), Pair(PolicyAction.EDIT, false))), Pair(NoEntity.GROUP, hashMapOf(Pair(PolicyAction.READ, true)))))
        val exchange = exchange(
                entity = HttpEntity<Unit>(testHeaders()),
                responseType = object : ParameterizedTypeReference<NoPageResponse<User>>() {},
                method = HttpMethod.GET, url = "$baseApiUrl${UserController.USERS_ENDPOINT}?id=$newId,$ownerId")
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertEquals(2, exchange.body!!.size)
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
        Assertions.assertEquals(HttpStatus.OK, exchange.statusCode)
        Assertions.assertEquals(1, exchange.body!!.size)
    }

}
