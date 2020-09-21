package net.nostalogic.datamodel.access

import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity
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

    fun addSubjects(grant: NoGrant): AccessQuery {
        if (grant is LoginGrant) {
            addSubject(EntityReference(grant.subject, NoEntity.USER))
            addSubjects(grant.additional.map { EntityReference(it, NoEntity.USER) })
        } else if (grant is ImpersonationGrant) {
            addSubject(EntityReference(grant.subject, NoEntity.USER))
            addSubjects(grant.additional.map { EntityReference(it, NoEntity.USER) })
        }
        return this
    }


    fun addQuery(resourceId: String?, entity: NoEntity, actions: HashSet<PolicyAction>): AccessQuery {
        val reference = EntityReference(resourceId, entity)
        if (!resourceQueries.containsKey(reference.toString()))
            resourceQueries[reference.toString()] = HashSet()
        resourceQueries[reference.toString()]!!.addAll(actions)
        return this
    }

}
