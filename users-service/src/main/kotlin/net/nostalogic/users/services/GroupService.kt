package net.nostalogic.users.services

import net.nostalogic.datamodel.access.AccessQuery
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.EntitySignature
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoAccessException
import net.nostalogic.exceptions.NoDeleteException
import net.nostalogic.exceptions.NoRetrieveException
import net.nostalogic.exceptions.NoSaveException
import net.nostalogic.security.contexts.SessionContext
import net.nostalogic.users.constants.GroupType
import net.nostalogic.users.datamodel.groups.Group
import net.nostalogic.users.datamodel.groups.GroupSearchCriteria
import net.nostalogic.users.mappers.GroupMapper
import net.nostalogic.users.persistence.entities.GroupEntity
import net.nostalogic.users.persistence.repositories.GroupRepository
import net.nostalogic.users.validators.GroupValidator
import net.nostalogic.utils.AutoPolicy
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class GroupService(
        @Autowired private val groupRepository: GroupRepository,
        @Autowired private val membershipService: MembershipService) {

    private val logger = LoggerFactory.getLogger(GroupService::class.java)

    fun getGroup(groupId: String): Group {
        if (!AccessQuery().simpleCheck(groupId, NoEntity.GROUP, PolicyAction.READ))
            throw NoAccessException(301007, "You do not have read permission for group $groupId")
        val groupEntity = groupRepository.findByIdOrNull(groupId) ?: throw NoRetrieveException(304009, "Group")
        if (groupEntity.status == EntityStatus.DELETED)
            throw NoRetrieveException(304011, "Group", "${EntitySignature(groupId, NoEntity.GROUP)} has been deleted")
        return GroupMapper.entityToDto(groupEntity)
    }

    fun getGroups(searchCriteria: GroupSearchCriteria): List<Group> {
        val groupIds = searchCriteria.groupIds
        val accessQuery = AccessQuery().currentSubject()
                .addQuery(null, NoEntity.GROUP, PolicyAction.READ)
        if (searchCriteria.groupIds.isNotEmpty())
            accessQuery.addQuery(searchCriteria.groupIds, NoEntity.GROUP, PolicyAction.READ)
        val report = accessQuery.toReport()

        val groupEntities: Page<GroupEntity> = if (groupIds.isEmpty()
                && report.hasPermission(EntityReference(entity = NoEntity.GROUP), PolicyAction.READ)) {
//            ArrayList(groupRepository.findAll())
            groupRepository.findAllByTypeInAndStatusIn(searchCriteria.type, searchCriteria.status, searchCriteria.page.toQuery())
        } else {
            val validIds: Collection<String> =
                    when {
                        report.hasPermission(EntityReference(entity = NoEntity.GROUP), PolicyAction.READ) -> groupIds
                        groupIds.isEmpty() -> report.resourcePermissions.map { EntityReference(it.key).id!! }.toHashSet()
                        else -> report.filterByPermitted(groupIds, NoEntity.GROUP, PolicyAction.READ)
                    }
//            ArrayList(groupRepository.findAllById(validIds))
            groupRepository.findAllByIdInAndTypeInAndStatusIn(validIds, searchCriteria.type, searchCriteria.status, searchCriteria.page.toQuery())
        }
        searchCriteria.page.hasNext = groupEntities.hasNext()

        return groupEntities.map { GroupMapper.entityToDto(it) }.toList()
    }

    fun updateGroup(groupId: String, update: Group): Group {
        if (!AccessQuery().simpleCheck(groupId, NoEntity.GROUP, PolicyAction.EDIT))
            throw NoAccessException(301010, "You do not have edit permission for group $groupId")

        val groupEntity = groupRepository.findByIdOrNull(groupId) ?: throw NoRetrieveException(304010, "Group")
        GroupValidator.validate(update, groupId, update.name?.let { groupRepository.findByNameEquals(it) })

        if (StringUtils.isNotBlank(update.name))
            groupEntity.name = update.name!!
        if (StringUtils.isNotBlank(update.description))
            groupEntity.description = update.description!!

        return GroupMapper.entityToDto(saveGroup(groupEntity))
    }

    fun deleteGroup(groupId: String, hard: Boolean = false): Group {
        if (!AccessQuery().simpleCheck(groupId, NoEntity.GROUP, PolicyAction.DELETE))
            throw NoAccessException(301008, "You do not have delete permission for group $groupId")
        val deletedGroups = groupRepository.updateGroupsStatus(setOf(groupId), EntityStatus.DELETED.name)
        if (deletedGroups.isEmpty())
            throw NoRetrieveException(304013, "Group")
        val deletedGroup = deletedGroups.first()
        if (hard)
            hardDeleteGroup(deletedGroup)
        return GroupMapper.entityToDto(deletedGroup)
    }

    private fun hardDeleteGroup(groupEntity: GroupEntity): GroupEntity {
        return try {
            membershipService.deleteAllUsersFromGroup(groupEntity.id)
            groupRepository.delete(groupEntity)
            groupEntity
        } catch (e: Exception) {
            logger.error("Unable to delete group ${groupEntity.id}", e)
            throw NoDeleteException(code = 303005, objectName = "group", cause = e)
        }
    }

    fun createGroup(group: Group): Group {
        if (!AccessQuery().simpleCheck(null, NoEntity.GROUP, PolicyAction.CREATE))
            throw NoAccessException(301009, "You do not have create permission for groups")
        GroupValidator.validate(group = group, groupByName = group.name?.let { groupRepository.findByNameEquals(it) }, isCreate = true)
        val userId = SessionContext.getUserId()

        val entity = saveGroup(GroupEntity(
                group.name!!,
                group.description,
                group.type ?: GroupType.USER,
                group.status ?: EntityStatus.INACTIVE,
                userId))
        val createdGroup = GroupMapper.entityToDto(entity)

        if (!group.permissions.isNullOrEmpty())
            AutoPolicy.savePermissions(EntitySignature(entity.id, NoEntity.GROUP), userId, group.permissions!!)

        return createdGroup
    }

    private fun saveGroup(groupEntity: GroupEntity): GroupEntity {
        return try {
            groupRepository.save(groupEntity)
        } catch (e: Exception) {
            logger.error("Unable to save group ${groupEntity.id}", e)
            throw NoSaveException(305003, "group", e)
        }
    }

}
