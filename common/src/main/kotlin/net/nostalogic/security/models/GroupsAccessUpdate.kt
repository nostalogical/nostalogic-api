package net.nostalogic.security.models

class GroupsAccessUpdate(
    val updaterId: String?,
    val tenant: String?,
    val groups: Set<String> = HashSet()
)
