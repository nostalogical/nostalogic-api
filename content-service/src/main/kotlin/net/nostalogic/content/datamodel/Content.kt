package net.nostalogic.content.datamodel

data class Content<T>(
    val path: String,
    val breadcrumbs: List<String>,
    val topLinks: List<Nav>,
    val sideLinks: List<Nav>,
    val type: ContainerType,
    val content: T
)
