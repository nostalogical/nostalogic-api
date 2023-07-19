package net.nostalogic.datamodel.access

import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity
import net.nostalogic.security.contexts.SessionContext

class SimpleAccess(
    id: String? = null,
    entity: NoEntity,
    val action: PolicyAction,
    creatorId: String? = null,
) {

    val ref = EntityReference(id, entity)
    val query = AccessQuery().currentSubject().addQuery(ref, action)
    val selfCheck = id == SessionContext.getUserId() || creatorId == SessionContext.getUserId()

    init {
        if (selfCheck && action == PolicyAction.EDIT)
            query.addQuery(ref, PolicyAction.EDIT_OWN)
        else if (selfCheck && action == PolicyAction.DELETE)
            query.addQuery(ref, PolicyAction.DELETE_OWN)
    }

    fun check(): Boolean {
        val report = query.toReport()
        var allowed = report.hasPermission(ref, action)
        if (!allowed && selfCheck) {
            if (action == PolicyAction.EDIT)
                allowed = report.hasPermission(ref, PolicyAction.EDIT_OWN)
            if (action == PolicyAction.DELETE)
                allowed = report.hasPermission(ref, PolicyAction.DELETE_OWN)
        }
        return allowed
    }

}
