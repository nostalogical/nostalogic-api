package net.nostalogic.content.datamodel.navigations

import com.fasterxml.jackson.annotation.JsonIgnore

data class NavDetails(
    @JsonIgnore var navId: String?,
    var fullPath: String?,
    var urn: String,
    var breadcrumbs: List<String>,
    var topLinks: List<Nav>,
    var sideLinks: List<Nav>,
    @JsonIgnore val system: Boolean? = false
)
