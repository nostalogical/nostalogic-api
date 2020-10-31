package net.nostalogic.users.datamodel

class MembershipChanges(
        val groupId: String,
        val usersAdded: Set<String> = HashSet(),
        val usersRemoved: Set<String> = HashSet(),
        val usersUnchanged: Map<String, String> = HashMap()
)
