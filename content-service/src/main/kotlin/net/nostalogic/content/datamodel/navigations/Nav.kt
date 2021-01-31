package net.nostalogic.content.datamodel.navigations

import net.nostalogic.content.datamodel.containers.Container
import net.nostalogic.entities.EntityStatus

data class Nav(
    var id: String? = null,
    var parentId: String? = null,
    var text: String?,
    var icon: String?,
    var path: String?,
    var status: EntityStatus? = EntityStatus.INACTIVE,
    var container: Container? = null
)
