package net.nostalogic.content.datamodel

data class NavDetails(
    var fullPath: String,
    var urn: String,
    var breadcrumbs: List<String>,
    var top: List<Nav>,
    var side: List<Nav>
)
