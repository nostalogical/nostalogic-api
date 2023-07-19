package net.nostalogic.users.services

import io.mockk.every
import io.mockk.mockk
import net.nostalogic.comms.AccessComms
import net.nostalogic.comms.Comms
import net.nostalogic.constants.NoLocale
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.AccessReport
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoValidationException
import net.nostalogic.users.config.UserUnitTestConfig
import net.nostalogic.users.datamodel.users.SecureUserUpdate
import net.nostalogic.users.persistence.entities.UserEntity
import net.nostalogic.users.persistence.repositories.UserRepository
import net.nostalogic.utils.EntityUtils
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles(profiles = ["test"])
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [UserUnitTestConfig::class])
class UserServiceTest(
    @Autowired private val userService: UserService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val userAuthService: UserAuthService,
) {

    @BeforeEach
    fun setup() {
        Comms.accessComms = this.accessComms
    }

    private val accessComms: AccessComms = mockk()
    private val userId = EntityUtils.uuid()

    private fun testUser(): UserEntity {
        return UserEntity(
            id = userId,
            username = "Test user",
            displayName = "Test user",
            email = "test@nostalogic.net",
            locale = NoLocale.en_GB,
            status = EntityStatus.ACTIVE,
        )
    }

    @Test
    fun `Passwords should have a size limit`() {
        val user = testUser()
        every { userRepository.findByIdEquals(userId) } answers { user }
        every { userAuthService.validateUserPassword(user, any()) } answers { true }

        val entityPermissions = hashMapOf(Pair(NoEntity.USER, hashMapOf(Pair(PolicyAction.EDIT, true), Pair(PolicyAction.EDIT_OWN, true))))
        every { accessComms.query(ofType(AccessQuery::class)) } answers {
            AccessReport(entityPermissions = entityPermissions) }

        val longPassword = RandomStringUtils.random(501)
        val update = SecureUserUpdate(password = longPassword, currentPassword = "passwordOld")

        assertThrows<NoValidationException>({ userService.secureUpdate(userId, update) }).let {
            assertEquals(307002, it.errorCode)
        }
    }

}
