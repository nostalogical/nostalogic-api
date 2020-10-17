package net.nostalogic.access.datamodel

import net.nostalogic.datamodel.access.Policy
import net.nostalogic.datamodel.access.PolicyAction
import net.nostalogic.datamodel.access.PolicyPriority
import net.nostalogic.entities.EntityReference

data class ResourcePermissionContext(
        val resource: EntityReference,
        val action: PolicyAction,
        var priority: PolicyPriority? = null,
        var allow: Boolean = false,
        val policies: HashMap<String, ContextPolicyDetails> = HashMap()) {

    private fun addPolicy(policy: Policy, subjects: HashSet<String>, resource: String, action: PolicyAction, replace: Boolean) {
        if (replace) {
            this.policies.clear()
        }
        this.policies.put(policy.id!!, ContextPolicyDetails(policy.name!!, subjects, hashSetOf(resource)))
        this.priority = policy.priority!!
        this.allow = policy.permissions!!.get(action)!!
    }

    fun updatePolicies(policy: Policy, subjects: HashSet<String>, resource: String, action: PolicyAction) {
        if (this.policies.isEmpty())
            addPolicy(policy, subjects, resource, action, false)
        else {
            if (this.policies.containsKey(policy.id!!))
                this.policies[policy.id!!]!!.resources.add(resource)
            else if (policy.priority!!.ordinal > this.priority!!.ordinal)
                addPolicy(policy, subjects, resource, action, true)
            else if (policy.priority!!.ordinal == this.priority!!.ordinal) {
                if (policy.permissions!![action] == this.allow)
                    addPolicy(policy, subjects, resource, action, false)
                else if (policy.permissions!![action] == false)
                    addPolicy(policy, subjects, resource, action, true)
            }
        }
    }

}
