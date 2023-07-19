package net.nostalogic.access.processors

import net.nostalogic.entities.EntityReference
import net.nostalogic.entities.NoEntity

class ResourceMatcher(
    val targetResource: EntityReference,
    val policyResource: EntityReference,
    ) {

    private fun isSignatureMatch(): Boolean {
        return policyResource == targetResource
    }

    private fun isEntityMatch(): Boolean {
        return policyResource.equals(targetResource.entity.name)
    }

    private fun isGlobalMatch(): Boolean {
        return policyResource.equals(NoEntity.ALL.name)
    }

    fun isMatch(): Boolean {
        return  isSignatureMatch() || isEntityMatch() || isGlobalMatch()
    }

}
