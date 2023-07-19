package net.nostalogic.datamodel.access

import net.nostalogic.comms.Comms
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity
import net.nostalogic.security.contexts.SessionContext
import net.nostalogic.security.grants.ImpersonationGrant
import net.nostalogic.security.grants.LoginGrant
import net.nostalogic.security.grants.NoGrant

data class AccessQuery(val subjects: HashSet<String> = HashSet(),
                       val resourceQueries:HashMap<String, HashSet<PolicyAction>> = HashMap()) {

    fun addSubject(subject: EntityReference): AccessQuery {
        this.subjects.add(subject.toString())
        return this
    }

    fun currentSubject(): AccessQuery {
        addSubjects(SessionContext.getGrant())
        return this
    }

    private fun addSubjects(grant: NoGrant): AccessQuery {
        if (grant is LoginGrant) {
            addSubject(EntityReference(grant.subject, NoEntity.USER))
        } else if (grant is ImpersonationGrant) {
            addSubject(EntityReference(grant.subject, NoEntity.USER))
        }
        return this
    }

    fun addQuery(entity: NoEntity, vararg actions: PolicyAction): AccessQuery {
        return addQuery(null, entity, *actions)
    }

    fun addQuery(ref: EntityReference, vararg actions: PolicyAction): AccessQuery {
        return addQuery(ref.id, ref.entity, *actions)
    }

    fun addQuery(resourceId: String? = null, entity: NoEntity, vararg actions: PolicyAction): AccessQuery {
        val reference = EntityReference(resourceId, entity)
        if (!resourceQueries.containsKey(reference.toString()))
            resourceQueries[reference.toString()] = HashSet()
        resourceQueries[reference.toString()]!!.addAll(actions)
        return this
    }

    fun addQuery(resourceId: Collection<String>, entity: NoEntity, vararg actions: PolicyAction): AccessQuery {
        for (id in resourceId) {
            val reference = EntityReference(id, entity)
            if (!resourceQueries.containsKey(reference.toString()))
                resourceQueries[reference.toString()] = HashSet()
            resourceQueries[reference.toString()]!!.addAll(actions)
        }
        return this
    }

    fun toReport(): AccessReport {
        return Comms.accessComms.query(this)
    }

    /**
     * Perform a simple check on a single entity's permissions.
     * A creator ID can also be specified, so if either this or the actual entity ID is the same as the current user,
     * the permissions are determined to be in the "own" category. If this is a check on edit or delete rights, the
     * "own" version of these actions is automatically included.
     */
    fun simpleCheck(id: String? = null, entity: NoEntity, action: PolicyAction, creatorId: String? = null): Boolean {
        val ref = EntityReference(id, entity)
        val query = currentSubject().addQuery(ref, action)

        val selfCheck = id == SessionContext.getUserId() || creatorId == SessionContext.getUserId()
        if (selfCheck && action == PolicyAction.EDIT)
            query.addQuery(ref, PolicyAction.EDIT_OWN)
        else if (selfCheck && action == PolicyAction.DELETE)
            query.addQuery(ref, PolicyAction.DELETE_OWN)

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
