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

    fun addSubjects(subjects: Collection<EntityReference>): AccessQuery {
        subjects.forEach { addSubject(it) }
        return this
    }

    fun currentSubject(): AccessQuery {
        addSubjects(SessionContext.getGrant())
        return this
    }

    private fun addSubjects(grant: NoGrant): AccessQuery {
        if (grant is LoginGrant) {
            addSubject(EntityReference(grant.subject, NoEntity.USER))
            addSubjects(grant.additional.map { EntityReference(it, NoEntity.USER) })
        } else if (grant is ImpersonationGrant) {
            addSubject(EntityReference(grant.subject, NoEntity.USER))
            addSubjects(grant.additional.map { EntityReference(it, NoEntity.USER) })
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

    fun simpleCheck(id: String? = null, entity: NoEntity, action: PolicyAction, creatorId: String? = null): Boolean {
        val ref = EntityReference(id, entity)
        val selfCheck = id == SessionContext.getUserId() || creatorId == SessionContext.getUserId()
        val actions = hashSetOf(action)

        val query = currentSubject().addQuery(ref, action)
        if (selfCheck && action == PolicyAction.EDIT)
            actions.add(PolicyAction.EDIT_OWN)
        else if (selfCheck && action == PolicyAction.DELETE)
            actions.add(PolicyAction.DELETE_OWN)

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
