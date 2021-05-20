package net.nostalogic.content.datamodel.navigations

import net.nostalogic.content.datamodel.containers.Container
import net.nostalogic.entities.EntityStatus

data class Nav(
    var id: String? = null,
    var parentId: String? = null,
    var text: String? = null,
    var icon: String? = null,
    var path: String? = null,
    var status: EntityStatus? = EntityStatus.INACTIVE,
    var container: Container? = null
)
