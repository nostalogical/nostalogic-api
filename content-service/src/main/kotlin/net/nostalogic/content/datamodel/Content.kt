package net.nostalogic.content.datamodel

import net.nostalogic.content.datamodel.navigations.Nav

data class Content<T>(
    val path: String,
    val breadcrumbs: List<String>,
    val topLinks: List<Nav>,
    val sideLinks: List<Nav>,
    val type: ContainerType,
    val content: T
)
