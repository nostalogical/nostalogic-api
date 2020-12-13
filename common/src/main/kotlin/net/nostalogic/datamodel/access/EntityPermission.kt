package net.nostalogic.datamodel.access

import net.nostalogic.datamodel.NamedEntity

data class EntityPermission(
        val action: PolicyAction,
        val hidden: Boolean?,
        val subjects: HashSet<NamedEntity>?)
