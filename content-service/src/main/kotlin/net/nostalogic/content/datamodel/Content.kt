package net.nostalogic.content.datamodel

import net.nostalogic.content.datamodel.navigations.NavLink

data class Content<T>(
    val path: String,
    val breadcrumbs: List<String>,
    val topLinks: List<NavLink>,
    val sideLinks: List<NavLink>,
    val type: ContainerType,
    val content: T
)
