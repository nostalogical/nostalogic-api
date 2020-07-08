package net.nostalogic.datamodel.access

import net.nostalogic.entities.EntityStatus
import java.util.*
import kotlin.collections.HashSet

class Policy(
        var id: String? = null,
        var status: EntityStatus = EntityStatus.ACTIVE,
        var name: String,
        var priority: PolicyPriority,
        val resources: HashSet<String> = HashSet(),
        val subjects: HashSet<String> = HashSet(),
        val permissions: EnumMap<PolicyAction, Boolean> = EnumMap(PolicyAction::class.java)
)
