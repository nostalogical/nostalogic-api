package net.nostalogic.users.datamodel

import net.nostalogic.entities.EntityStatus

class User(
        val id: String?,
        var username: String?,
        var email: String?,
        var status: EntityStatus) {
}
