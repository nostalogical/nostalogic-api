package net.nostalogic.users.services

import io.mockk.every
import io.mockk.mockk
import net.nostalogic.users.config.UserUnitTestConfig
import net.nostalogic.users.constants.GroupType
import net.nostalogic.users.constants.MembershipRole
import net.nostalogic.users.constants.MembershipStatus
import net.nostalogic.users.datamodel.memberships.GroupMembershipChanges
import net.nostalogic.users.persistence.entities.MembershipEntity
import net.nostalogic.users.persistence.repositories.MembershipRepository
import net.nostalogic.utils.EntityUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles(profiles = ["test"])
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [UserUnitTestConfig::class])
class MembershipServiceTest(
        @Autowired private val membershipService: MembershipService,
        @Autowired private val membershipRepository: MembershipRepository
) {

    private val userId = EntityUtils.uuid()
    private val groupId = EntityUtils.uuid()

    @BeforeEach
    fun setup() {
        every { membershipRepository.delete(any()) } answers { mockk() }
        every { membershipRepository.save(ofType(MembershipEntity::class)) } answers { firstArg() }
    }

    private fun changes(): GroupMembershipChanges {
        return GroupMembershipChanges(groupId)
    }

    private fun membership(
        groupType: GroupType = GroupType.USER,
        status: MembershipStatus = MembershipStatus.ACTIVE,
        rightsGroup: Boolean = false,
    ): MembershipEntity {
        return MembershipEntity(
            userId = userId,
            groupId = groupId,
            groupType = groupType,
            rightsGroup = rightsGroup,
            status = status,
            role = MembershipRole.REGULAR,
            creatorId = EntityUtils.uuid()
        )
    }

    @Test
    fun `Remove membership of editable user`() {
        val changes = changes()
        membershipService.processMembershipRemoval(changes, membership(), userId, manageGroup = false, editUser = true)
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertTrue(changes.memberships.first().changed)
        Assertions.assertEquals(MembershipStatus.ACTIVE, changes.memberships.first().oldStatus)
        Assertions.assertNull(changes.memberships.first().newStatus)
        Assertions.assertNull(changes.memberships.first().failReason)
    }

    @Test
    fun `Remove membership of editable group`() {
        val changes = changes()
        membershipService.processMembershipRemoval(changes, membership(), userId, manageGroup = true, editUser = false)
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertTrue(changes.memberships.first().changed)
        Assertions.assertEquals(MembershipStatus.ACTIVE, changes.memberships.first().oldStatus)
        Assertions.assertNull(changes.memberships.first().newStatus)
        Assertions.assertNull(changes.memberships.first().failReason)
    }

    @Test
    fun `Remove membership with no edit right`() {
        val changes = changes()
        membershipService.processMembershipRemoval(
            changes = changes,
            existingMembership = membership(),
            userId = userId,
            manageGroup = false,
            editUser = false
        )
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertFalse(changes.memberships.first().changed)
        Assertions.assertEquals(MembershipStatus.ACTIVE, changes.memberships.first().oldStatus)
        Assertions.assertEquals(MembershipStatus.ACTIVE, changes.memberships.first().newStatus)
        Assertions.assertNotNull(changes.memberships.first().failReason)
    }

    @Test
    fun `Remove membership from rights group with edit group rights`() {
        val changes = changes()
        membershipService.processMembershipRemoval(
            changes = changes,
            existingMembership = membership(
                rightsGroup = true
            ),
            userId = userId,
            manageGroup = true,
            editUser = false
        )
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertTrue(changes.memberships.first().changed)
        Assertions.assertEquals(MembershipStatus.ACTIVE, changes.memberships.first().oldStatus)
        Assertions.assertNull(changes.memberships.first().newStatus)
        Assertions.assertNull(changes.memberships.first().failReason)
    }

    @Test
    fun `Remove membership from rights group with edit user rights`() {
        val changes = changes()
        membershipService.processMembershipRemoval(
            changes = changes,
            existingMembership = membership(rightsGroup = true),
            userId = userId,
            manageGroup = false,
            editUser = true
        )
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertFalse(changes.memberships.first().changed)
        Assertions.assertEquals(MembershipStatus.ACTIVE, changes.memberships.first().oldStatus)
        Assertions.assertEquals(MembershipStatus.ACTIVE, changes.memberships.first().newStatus)
        Assertions.assertNotNull(changes.memberships.first().failReason)
    }

    @Test
    fun `Remove membership of group chief`() {
        val changes = changes()
        val membership = membership()
        membership.role = MembershipRole.OWNER
        membershipService.processMembershipRemoval(changes, membership, userId, manageGroup = true, editUser = true)
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertFalse(changes.memberships.first().changed)
        Assertions.assertEquals(MembershipStatus.ACTIVE, changes.memberships.first().oldStatus)
        Assertions.assertEquals(MembershipStatus.ACTIVE, changes.memberships.first().newStatus)
        Assertions.assertNotNull(changes.memberships.first().failReason)
    }

    @Test
    fun `Remove membership that doesn't exist`() {
        val changes = changes()
        membershipService.processMembershipRemoval(changes, null, userId, manageGroup = true, editUser = true)
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertFalse(changes.memberships.first().changed)
        Assertions.assertNull(changes.memberships.first().oldStatus)
        Assertions.assertNull(changes.memberships.first().newStatus)
        Assertions.assertNotNull(changes.memberships.first().failReason)
    }

    @Test
    fun `Add membership of non-member user with full rights`() {
        val changes = changes()
        membershipService.processMembershipAddition(changes, null, userId, groupId, manageGroup = true, editUser = true)
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertTrue(changes.memberships.first().changed)
        Assertions.assertNull(changes.memberships.first().oldStatus)
        Assertions.assertEquals(MembershipStatus.ACTIVE, changes.memberships.first().newStatus)
        Assertions.assertNull(changes.memberships.first().failReason)
    }

    @Test
    fun `Add membership of non-member user with no rights`() {
        val changes = changes()
        membershipService.processMembershipAddition(changes, null, userId, groupId, manageGroup = false, editUser = false)
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertFalse(changes.memberships.first().changed)
        Assertions.assertNull(changes.memberships.first().oldStatus)
        Assertions.assertNull(changes.memberships.first().newStatus)
        Assertions.assertNotNull(changes.memberships.first().failReason)
    }

    @Test
    fun `Add membership application of non-member user with user edit rights`() {
        val changes = changes()
        membershipService.processMembershipAddition(changes, null, userId, groupId, manageGroup = false, editUser = true)
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertTrue(changes.memberships.first().changed)
        Assertions.assertNull(changes.memberships.first().oldStatus)
        Assertions.assertEquals(MembershipStatus.APPLIED, changes.memberships.first().newStatus)
        Assertions.assertNull(changes.memberships.first().failReason)
    }

    @Test
    fun `Add membership invitation of non-member user with group management rights`() {
        val changes = changes()
        membershipService.processMembershipAddition(changes, null, userId, groupId, manageGroup = true, editUser = false)
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertTrue(changes.memberships.first().changed)
        Assertions.assertNull(changes.memberships.first().oldStatus)
        Assertions.assertEquals(MembershipStatus.INVITED, changes.memberships.first().newStatus)
        Assertions.assertNull(changes.memberships.first().failReason)
    }

    @Test
    fun `Add invitation acceptance with user edit rights`() {
        val changes = changes()
        membershipService.processMembershipAddition(changes, membership(status = MembershipStatus.INVITED), userId, groupId, manageGroup = false, editUser = true)
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertTrue(changes.memberships.first().changed)
        Assertions.assertEquals(MembershipStatus.INVITED, changes.memberships.first().oldStatus)
        Assertions.assertEquals(MembershipStatus.ACTIVE, changes.memberships.first().newStatus)
        Assertions.assertNull(changes.memberships.first().failReason)
    }

    @Test
    fun `Add application acceptance with group edit rights`() {
        val changes = changes()
        membershipService.processMembershipAddition(changes, membership(status = MembershipStatus.APPLIED), userId, groupId, manageGroup = true, editUser = false)
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertTrue(changes.memberships.first().changed)
        Assertions.assertEquals(MembershipStatus.APPLIED, changes.memberships.first().oldStatus)
        Assertions.assertEquals(MembershipStatus.ACTIVE, changes.memberships.first().newStatus)
        Assertions.assertNull(changes.memberships.first().failReason)
    }

    @Test
    fun `Add membership for an applied user with user edit rights`() {
        val changes = changes()
        membershipService.processMembershipAddition(changes, membership(status = MembershipStatus.APPLIED), userId, groupId, manageGroup = false, editUser = true)
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertFalse(changes.memberships.first().changed)
        Assertions.assertEquals(MembershipStatus.APPLIED, changes.memberships.first().oldStatus)
        Assertions.assertEquals(MembershipStatus.APPLIED, changes.memberships.first().newStatus)
        Assertions.assertNotNull(changes.memberships.first().failReason)
    }

    @Test
    fun `Add membership for an invited user with group edit rights`() {
        val changes = changes()
        membershipService.processMembershipAddition(changes, membership(status = MembershipStatus.INVITED), userId, groupId, manageGroup = true, editUser = false)
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertFalse(changes.memberships.first().changed)
        Assertions.assertEquals(MembershipStatus.INVITED, changes.memberships.first().oldStatus)
        Assertions.assertEquals(MembershipStatus.INVITED, changes.memberships.first().newStatus)
        Assertions.assertNotNull(changes.memberships.first().failReason)
    }

    @Test
    fun `Add membership for a suspended user`() {
        val changes = changes()
        membershipService.processMembershipAddition(changes, membership(status = MembershipStatus.SUSPENDED), userId, groupId, manageGroup = true, editUser = true)
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertFalse(changes.memberships.first().changed)
        Assertions.assertEquals(MembershipStatus.SUSPENDED, changes.memberships.first().oldStatus)
        Assertions.assertEquals(MembershipStatus.SUSPENDED, changes.memberships.first().newStatus)
        Assertions.assertNotNull(changes.memberships.first().failReason)
    }

    @Test
    fun `Add membership for an active user`() {
        val changes = changes()
        membershipService.processMembershipAddition(changes, membership(status = MembershipStatus.ACTIVE), userId, groupId, manageGroup = true, editUser = true)
        Assertions.assertEquals(1, changes.memberships.size)
        Assertions.assertFalse(changes.memberships.first().changed)
        Assertions.assertEquals(MembershipStatus.ACTIVE, changes.memberships.first().oldStatus)
        Assertions.assertEquals(MembershipStatus.ACTIVE, changes.memberships.first().newStatus)
        Assertions.assertNotNull(changes.memberships.first().failReason)
    }

}
