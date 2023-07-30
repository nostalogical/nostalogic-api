package net.nostalogic.users.services

import net.nostalogic.comms.Comms
import net.nostalogic.constants.NoStrings
import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoAccessException
import net.nostalogic.exceptions.NoDeleteException
import net.nostalogic.exceptions.NoRetrieveException
import net.nostalogic.exceptions.NoSaveException
import net.nostalogic.security.contexts.SessionContext
import net.nostalogic.users.constants.GroupType
import net.nostalogic.users.constants.MembershipRole
import net.nostalogic.users.constants.MembershipStatus
import net.nostalogic.users.datamodel.memberships.GroupMembershipChanges
import net.nostalogic.users.datamodel.memberships.Membership
import net.nostalogic.users.datamodel.memberships.MembershipSearchCriteria
import net.nostalogic.users.mappers.MembershipMapper
import net.nostalogic.users.persistence.entities.MembershipEntity
import net.nostalogic.users.persistence.repositories.GroupRepository
import net.nostalogic.users.persistence.repositories.MembershipRepository
import net.nostalogic.users.persistence.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class MembershipService(
        @Autowired private val membershipRepository: MembershipRepository,
        @Autowired private val userRepository: UserRepository,
        @Autowired private val groupRepository: GroupRepository,
    ) {

    private val logger = LoggerFactory.getLogger(MembershipService::class.java)

    fun getGroupsForUserRights(userId: String): Set<String> {
        val groups = membershipRepository.findAllByUserIdEqualsAndStatusIn(userId, setOf(MembershipStatus.ACTIVE))
        return groups.stream().map { it.groupId }.collect(Collectors.toSet())
    }

    fun deleteUserFromAllGroups(userId: String) {
        try {
            membershipRepository.deleteAllByUserId(userId)
        } catch (e: Exception) {
            logger.error("Unable to delete user memberships $userId", e)
            throw NoDeleteException(code = 303003, objectName = "membership", cause = e)
        }
    }

    fun deleteAllUsersFromGroup(groupId: String) {
        try {
            membershipRepository.deleteAllByGroupId(groupId)
        } catch (e: Exception) {
            logger.error("Unable to delete group memberships $groupId", e)
            throw NoDeleteException(code = 303004, objectName = "membership", cause = e)
        }
    }

    fun updateMembership(update: Membership, userId: String, groupId: String): Membership {
        SessionContext.requireLogin()
        val memberships = membershipRepository.findAllByUserIdInAndGroupIdIn(setOf(userId), setOf(groupId))
        if (memberships.isNullOrEmpty())
            throw NoRetrieveException(304016, "Membership", "No membership found for user $userId in group $groupId")
        val membership = memberships.first()

        var manageGroup = AccessQuery().simpleCheck(groupId, NoEntity.GROUP, PolicyAction.EDIT,
                groupRepository.findByIdOrNull(groupId)?.creatorId)
        if (!manageGroup) {
            val currentUserMember = membershipRepository.findAllByUserIdInAndGroupIdIn(setOf(SessionContext.getUserId()), setOf(groupId))
            if (!currentUserMember.isEmpty())
                manageGroup = setOf(MembershipRole.OWNER, MembershipRole.MANAGER).contains(currentUserMember.first().role)
        }

        if (!manageGroup)
            throw NoAccessException(301003, "Missing permissions to modify memberships for group $groupId")

        val requiresSessionUpdate = (membership.status == MembershipStatus.ACTIVE || update.status == MembershipStatus.ACTIVE) && membership.status != update.status

        if (update.role != null)
            membership.role = update.role
        if (update.status != null)
            membership.status = update.status

        try {
            val updated = MembershipMapper.entityToDto(membershipRepository.save(membership))
            if (requiresSessionUpdate)
                updateGroupsInSessions(setOf(userId))
            return updated
        } catch (e: Exception) {
            throw NoSaveException(305005, "membership", e)
        }
    }

    fun addUsersToGroups(userIds: Collection<String>, groupIds: Collection<String>): List<GroupMembershipChanges> {
        return processMembershipChanges(userIds, groupIds, object : MembershipsProcessor {
            override fun process(changes: GroupMembershipChanges, existingMembership: MembershipEntity?, userId: String, groupId: String, manageGroup: Boolean, editUser: Boolean) {
                processMembershipAddition(changes, existingMembership, userId, groupId, manageGroup, editUser)
            }
        })
    }

    fun removeUsersFromGroups(userIds: Collection<String>, groupIds: Collection<String>): List<GroupMembershipChanges> {
        return processMembershipChanges(userIds, groupIds, object : MembershipsProcessor {
            override fun process(changes: GroupMembershipChanges, existingMembership: MembershipEntity?, userId: String, groupId: String, manageGroup: Boolean, editUser: Boolean) {
                processMembershipRemoval(changes, existingMembership, userId, manageGroup, editUser)
            }
        })
    }

    private fun processMembershipChanges(userIds: Collection<String>, groupIds: Collection<String>, processor: MembershipsProcessor): List<GroupMembershipChanges> {
        SessionContext.requireLogin()
        val currentUserId = SessionContext.getUserId()

        // Confirm the groups and users exist
        val users = if (userIds.isEmpty()) emptyList()
        else userRepository.findAllById(userIds).filter { it.status != EntityStatus.DELETED }
        val groups = if (groupIds.isEmpty()) emptyList()
        else groupRepository.findAllById(groupIds).filter { it.status != EntityStatus.DELETED }
        if (groups.isEmpty()) throw NoRetrieveException(304014, "Group", "None of the specified groups were found")
        if (users.isEmpty()) throw NoRetrieveException(304015, "User", "None of the specified users were found")

        // Filter Ids to those that are present in the DB
        val existingGroupIds = groups.map { it.id }.toHashSet()
        val existingUserIds = users.map { it.id }.toHashSet()
        existingUserIds.add(currentUserId)

        // Map existing memberships by IDs
        val memberships = membershipRepository.findAllByUserIdInAndGroupIdIn(existingUserIds, existingGroupIds)
        val membershipByUserByGroup = HashMap<String, HashMap<String, MembershipEntity>>()
        memberships.forEach {
            membershipByUserByGroup.getOrPut(it.groupId) { HashMap() }[it.userId] = it
        }

        val changes: ArrayList<GroupMembershipChanges> = ArrayList()

        val report = AccessQuery().currentSubject()
                .addQuery(existingUserIds, NoEntity.USER, PolicyAction.EDIT)
                .addQuery(existingGroupIds, NoEntity.GROUP, PolicyAction.EDIT).toReport()

        groups.forEach { group ->
            val groupChanges = GroupMembershipChanges(groupId = group.id)
            changes.add(groupChanges)
            val editGroup = report.hasPermission(EntityReference(group.id, NoEntity.GROUP), PolicyAction.EDIT)
                    || currentUserId == group.creatorId && report.hasPermission(EntityReference(group.id, NoEntity.GROUP), PolicyAction.EDIT_OWN)
            val manageGroup = editGroup || setOf(MembershipRole.OWNER, MembershipRole.MANAGER).contains(membershipByUserByGroup[group.id]?.get(currentUserId)?.role)
            users.forEach { user ->
                val editUser = report.hasPermission(EntityReference(user.id, NoEntity.USER), PolicyAction.EDIT)
                        || currentUserId == user.id && report.hasPermission(EntityReference(user.id, NoEntity.USER), PolicyAction.EDIT_OWN)
                val existingMembership = membershipByUserByGroup[group.id]?.get(user.id)
                processor.process(groupChanges, existingMembership, user.id, group.id, manageGroup, editUser)
            }
        }

        val changedUsers = HashSet<String>()
        changes.forEach{ g -> g.memberships.forEach {
            if (it.changed)
                changedUsers.add(it.userId!!)
        } }
        if (changedUsers.isNotEmpty())
            updateGroupsInSessions(changedUsers)


        return changes
    }

    fun processMembershipAddition(changes: GroupMembershipChanges, existingMembership: MembershipEntity?,
                                  userId: String, groupId: String, manageGroup: Boolean, editUser: Boolean) {
        val currentUserId = SessionContext.getUserId()

        // Max membership status current user can set
        val newStatus = when {
            manageGroup && editUser -> MembershipStatus.ACTIVE
            editUser -> MembershipStatus.APPLIED
            manageGroup -> MembershipStatus.INVITED
            else -> null
        }

        if (newStatus == null)
            changes.addMembershipChange(userId, oldStatus = existingMembership?.status, reasonKey = NoStrings.membershipPermissions())
        else if (newStatus == existingMembership?.status)
            changes.addMembershipChange(userId, oldStatus = existingMembership.status, reasonKey = NoStrings.membershipUnchanged())
        else if (existingMembership?.status == MembershipStatus.ACTIVE)
            changes.addMembershipChange(userId, oldStatus = existingMembership.status, reasonKey = NoStrings.alreadyMember())
        else if (existingMembership?.status == MembershipStatus.SUSPENDED)
            changes.addMembershipChange(userId, oldStatus = existingMembership.status, reasonKey = NoStrings.alreadySuspended())
        else {
            if (existingMembership == null) {
                try {
                    membershipRepository.save(MembershipEntity(
                            userId = userId,
                            groupId = groupId,
                            status = newStatus,
                            role = MembershipRole.REGULAR,
                            creatorId = currentUserId))
                } catch (e: Exception) {
                    throw NoSaveException(305004, "membership", e)
                }
                changes.addMembershipChange(userId, changed = true, newStatus = newStatus)
                if (newStatus == MembershipStatus.ACTIVE)
                    logger.info("User {} added directly to group {} by user {}", userId, groupId, currentUserId)
                else
                    logger.info("User {} {} to group {} by user {}", userId, newStatus.name, groupId, currentUserId)
            } else {
                changes.addMembershipChange(userId, changed = true, oldStatus = existingMembership.status, newStatus = MembershipStatus.ACTIVE)
                existingMembership.status = MembershipStatus.ACTIVE
                if (existingMembership.status == MembershipStatus.APPLIED && newStatus == MembershipStatus.INVITED)
                    logger.info("User {} group application to {} accepted by user {}", userId, groupId, currentUserId)
                else if (existingMembership.status == MembershipStatus.INVITED && newStatus == MembershipStatus.APPLIED)
                    logger.info("User {} group invite to {} accepted by user {}", userId, groupId, currentUserId)
            }
        }
    }

    fun processMembershipRemoval(
        changes: GroupMembershipChanges,
        existingMembership: MembershipEntity?,
        userId: String,
        manageGroup: Boolean,
        editUser: Boolean
    ) {
        if (existingMembership == null)
            changes.addMembershipChange(userId, reasonKey = NoStrings.notMember())
        else if (existingMembership.role == MembershipRole.OWNER)
            changes.addMembershipChange(userId, oldStatus = existingMembership.status, reasonKey = NoStrings.ownerCannotLeave())
        else if (existingMembership.rightsGroup == true && !manageGroup)
            changes.addMembershipChange(userId, oldStatus = existingMembership.status, reasonKey = NoStrings.cannotRemoveFromRightsGroup())
        else if (!manageGroup && !editUser)
            changes.addMembershipChange(userId, oldStatus = existingMembership.status, reasonKey = NoStrings.cannotRemoveFromGroup())
        else {
            try {
                membershipRepository.delete(existingMembership)
                changes.addMembershipChange(userId, true, existingMembership.status, null)
            } catch (e: Exception) {
                throw NoDeleteException(303006, "membership")
            }
        }
    }

    fun getMemberships(searchCriteria: MembershipSearchCriteria, showUsers: Boolean = true, showGroups: Boolean = true): List<Membership> {
        val query = AccessQuery().currentSubject()
                .addQuery(null, NoEntity.USER, PolicyAction.READ)
                .addQuery(null, NoEntity.GROUP, PolicyAction.READ)
        if (searchCriteria.userIds.isNotEmpty())
            query.addQuery(searchCriteria.userIds, NoEntity.USER, PolicyAction.READ)
        if (searchCriteria.groupIds.isNotEmpty())
            query.addQuery(searchCriteria.groupIds, NoEntity.GROUP, PolicyAction.READ)
        val report = query.toReport()

        val userIds: Set<String>? = if (searchCriteria.userIds.isEmpty()
                && report.hasPermission(EntityReference(entity = NoEntity.USER), PolicyAction.READ)) null
        else report.filterByPermitted(searchCriteria.userIds, NoEntity.USER, PolicyAction.READ)
        val groupIds: Set<String>? = if (searchCriteria.groupIds.isEmpty()
                && report.hasPermission(EntityReference(entity = NoEntity.GROUP), PolicyAction.READ)) null
        else report.filterByPermitted(searchCriteria.groupIds, NoEntity.GROUP, PolicyAction.READ)

        val types = searchCriteria.type.map { it.name }.toSet()
        val isRights: Set<Boolean> = searchCriteria.rights?.let { setOf(it) } ?: setOf(true, false)
        val status = searchCriteria.status.map { it.name }.toSet()
        val page = searchCriteria.page.toQuery()

        val membershipEntities =
                when {
                    userIds == null && groupIds == null -> membershipRepository.searchMemberships(types, isRights, status, page)
                    userIds?.isEmpty() == true || groupIds?.isEmpty() == true -> return emptyList()
                    groupIds == null && userIds!!.isNotEmpty() -> membershipRepository.searchMembershipsForUsers(userIds, types, isRights, status, page)
                    userIds == null && groupIds!!.isNotEmpty() -> membershipRepository.searchMembershipsForGroups(groupIds, types, isRights, status, page)
                    else -> membershipRepository.searchMembershipsForUsersAndGroups(userIds!!, groupIds!!, types, isRights, status, page)
                }
        searchCriteria.page.setResponseMetadata(membershipEntities)

        return membershipEntities.map {
            when {
                showUsers == showGroups -> MembershipMapper.entityToDto(it)
                showGroups -> MembershipMapper.entityToGroupMemberDto(it)
                else -> MembershipMapper.entityToUserMemberDto(it)
            }
        }.toList()

    }

    private fun updateGroupsInSessions(userIds: Set<String>) {
        try {
            val activeMemberships = membershipRepository.getActiveGroupsForUsers(userIds)
            val memberGroups: Map<String, HashSet<String>> = userIds.map { it to HashSet<String>() }.toMap()
            activeMemberships.forEach { memberGroups[it.userId]?.add(it.groupId) }
            memberGroups.forEach {
                logger.info("Updating groups for user ${it.key} sessions")
                Comms.access().updateSession(it.value, it.key)
            }
        } catch (e: Exception) {
            logger.error("Unable to to session updates for changed user groups", e)
        }
    }

    interface MembershipsProcessor {
        fun process(changes: GroupMembershipChanges, existingMembership: MembershipEntity?,
                    userId: String, groupId: String, manageGroup: Boolean, editUser: Boolean)
    }

}
