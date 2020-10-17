package net.nostalogic.access.datamodel

data class ContextPolicyDetails (val name: String, val subjects: HashSet<String>, val resources: HashSet<String>)
