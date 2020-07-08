package net.nostalogic.datamodel.access

import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity

class AccessQuery(val token: String) {

    val resourceQueries = HashMap<EntityReference, HashSet<PolicyAction>>()
    val entityQueries = HashMap<NoEntity, HashSet<PolicyAction>>()

    fun addQuery(resource: String?, entity: NoEntity, actions: HashSet<PolicyAction>) {
        val reference = EntityReference(resource, entity)
        if (!resourceQueries.containsKey(reference))
            resourceQueries[reference] = HashSet()
        resourceQueries[reference]!!.addAll(actions)
    }

}
