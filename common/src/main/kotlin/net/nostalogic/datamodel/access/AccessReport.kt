package net.nostalogic.datamodel.access

import net.nostalogic.config.i18n.Translator
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoAccessException

class AccessReport(
        val subjectId: String?,
        val resourcePermissions: Map<String, Map<PolicyAction, Boolean>>,
        val entityPermissions: Map<NoEntity, Map<PolicyAction, Boolean>>
) {

    fun hasPermission(reference: EntityReference, action: PolicyAction): Boolean {
        var permission: Boolean? = resourcePermissions[reference.toEntityReference()]?.get(action)
        if (permission == null)
            permission = entityPermissions[reference.entity]?.get(action)
        if (permission != null)
            return permission
        else
            throw NoAccessException(101001,
                    "Action '${action}' not specified for '${reference.toEntityReference()}'",
                    Translator.translate("permissionMissing"))
    }
}
