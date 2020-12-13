package net.nostalogic.datamodel.access

import net.nostalogic.config.i18n.Translator
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity
import net.nostalogic.exceptions.NoAccessException

data class AccessReport(
        val subjectIds: Set<String> = emptySet(),
        val resourcePermissions: HashMap<String, HashMap<PolicyAction, Boolean>> = HashMap(),
        val entityPermissions: HashMap<NoEntity, HashMap<PolicyAction, Boolean>> = HashMap()
) {

    fun hasPermission(reference: EntityReference, action: PolicyAction): Boolean {
        var permission: Boolean? = resourcePermissions[reference.toString()]?.get(action)
        if (permission == null)
            permission = entityPermissions[reference.entity]?.get(action)
        if (permission != null)
            return permission
        else
            throw NoAccessException(101001,
                    "Action '${action}' not specified for '${reference}'",
                    Translator.translate("permissionMissing"))
    }

    fun filterByPermitted(ids: Collection<String>, entity: NoEntity, action: PolicyAction): Set<String> {
        if (entityPermissions[entity]?.get(action) == true)
            return ids.toHashSet()
        val permitted = HashSet<String>()
        ids.forEach {
            if (resourcePermissions[EntityReference(it, entity).toString()]?.containsKey(action) == true)
                permitted.add(it)
        }
        return permitted
    }
}
