package net.nostalogic.datamodel.access

import net.nostalogic.entities.EntityStatus
import java.util.*
import kotlin.collections.HashSet

data class Policy(
        var id: String? = null,
        var status: EntityStatus? = EntityStatus.ACTIVE,
        var name: String? = null,
        var priority: PolicyPriority? = null,
        var resources: HashSet<String>? = HashSet(),
        var subjects: HashSet<String>? = HashSet(),
        var permissions: EnumMap<PolicyAction, Boolean>? = EnumMap(PolicyAction::class.java)
)
