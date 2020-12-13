package net.nostalogic.users.validators

import net.nostalogic.entities.EntityStatus
import net.nostalogic.users.datamodel.groups.Group
import net.nostalogic.users.persistence.entities.GroupEntity
import net.nostalogic.validators.InvalidFieldsReport
import org.apache.commons.lang3.StringUtils

object GroupValidator {

    private const val DESCRIPTION_MAX = 3000

    fun validate(group: Group, groupId: String? = null, groupByName: GroupEntity? = null, isCreate: Boolean = false) {
        val report = InvalidFieldsReport()

        if (isCreate) {
            if (StringUtils.isBlank(group.name))
                report.addMissingField("name")
        }
        if (groupByName != null && groupByName.id != groupId)
            report.addFieldAlreadyInUse("name")

        if (StringUtils.isNotBlank(group.description) && group.description!!.length > DESCRIPTION_MAX)
            report.addFieldTooLong("description", DESCRIPTION_MAX)
        if (group.status == EntityStatus.DELETED)
            report.addInvalidFieldValue("description", EntityStatus.DELETED.name)
        report.validate(307006)
    }

}
