package net.nostalogic.utils

import net.nostalogic.comms.Comms
import net.nostalogic.constants.ExceptionCodes._0101003
import net.nostalogic.constants.ExceptionCodes._0101004
import net.nostalogic.constants.ExceptionCodes._0101005
import net.nostalogic.datamodel.NamedEntity
import net.nostalogic.datamodel.access.EntityPermission
import net.nostalogic.datamodel.access.Policy
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.datamodel.access.PolicyPriority
import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.EntitySignature
import net.nostalogic.entities.EntityStatus
import net.nostalogic.exceptions.NoAccessException

object AutoPolicy {

    fun generate(resource: EntitySignature, creator: String, action: PolicyAction, subjects: Set<EntityReference>, allow: Boolean = false): Policy {
        return Comms.access().createPolicy(Policy(
                status = EntityStatus.ACTIVE,
                name = autoPolicyName(resource, action),
                priority = PolicyPriority.TWO_STANDARD,
                resources = hashSetOf(resource.toString()),
                subjects = subjects.map { it.toString() }.toHashSet(),
                permissions = CollUtils.enumMapOf(Pair(action, allow)),
                creator = creator)) ?: throw NoAccessException(_0101003, "Unable to autogenerate policy for $resource")
    }

    fun retrieve(resource: EntitySignature, actions: Collection<PolicyAction>): Collection<Policy> {
        val expectedNames = actions.map { autoPolicyName(resource, it) }.toHashSet()
        val policies = Comms.access().retrievePolicies(resources = setOf(resource.toString()), status = EntityStatus.values().toSet(),
                priority = setOf(PolicyPriority.TWO_STANDARD), actions = actions.toHashSet())
                ?: throw NoAccessException(_0101004, "Unable to retrieve auto policies for $resource")
        policies.toHashSet().removeIf { !expectedNames.contains(it.name) }
        return policies
    }

    fun updatePolicy(policy: Policy): Policy {
        return Comms.access().updatePolicy(policy) ?: throw NoAccessException(_0101005, "Unable to update auto policy for ${policy.resources?.iterator()?.next()}")
    }

    fun delete(policyId: String) {
        Comms.access().deletePolicy(policyId, true)
    }

    private fun autoPolicyName(resource: EntitySignature, action: PolicyAction): String {
        return "AUTO - $resource - ${action.name}"
    }

    fun savePermissions(resource: EntitySignature, creator: String, permissions: Collection<EntityPermission>) {
        val existingPolicies = retrieve(resource, permissions.map { it.action }.toSet())
        val permissionsByAction = HashMap(permissions.map { it.action to it }.toMap())
        for (policy in existingPolicies) {
            val action = policy.permissions!!.keys.iterator().next()
            if (!permissionsByAction.keys.contains(action))
                delete(policy.id!!)
            else {
                policy.status = EntityStatus.ACTIVE
                policy.resources = hashSetOf(resource.toString())
                policy.name = autoPolicyName(resource, action)
                policy.priority = PolicyPriority.TWO_STANDARD
                policy.subjects = subjectsForPermission(permissionsByAction[action]!!).map { it.toString() }.toHashSet()
                updatePolicy(policy)
                permissionsByAction.remove(action)
            }
        }

        for (act in permissionsByAction)
            generate(resource, creator, act.key, subjectsForPermission(act.value), act.value.hidden != false)
    }

    fun retrievePermissions(resource: EntitySignature, actions: Set<PolicyAction> = emptySet()): Collection<EntityPermission> {
        val acts = if (actions.isNullOrEmpty()) PolicyAction.values().toHashSet() else actions
        val existingPolicies = retrieve(resource, acts)

        val permissions = HashSet<EntityPermission>()
        for (policy in existingPolicies)
            permissions.add(policyToPermission(policy))

        return permissions
    }

    private fun subjectsForPermission(permission: EntityPermission): Set<EntityReference> {
        return if (permission.hidden == true || permission.subjects.isNullOrEmpty()) HashSet()
            else permission.subjects.map { EntityReference(it.id, it.entity) }.toHashSet()
    }

    private fun policyToPermission(policy: Policy): EntityPermission {
        val hidden = policy.subjects.isNullOrEmpty()
        val subjects = policy.subjects!!.map {
            val ref = EntityReference(it)
            NamedEntity(null, ref.id, ref.entity)
        }.toHashSet()
        return EntityPermission(
                action = policy.permissions!!.keys.iterator().next(),
                hidden = hidden,
                subjects = subjects)
    }

}
