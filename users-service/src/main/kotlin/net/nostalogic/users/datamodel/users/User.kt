package net.nostalogic.users.datamodel.users

import com.fasterxml.jackson.annotation.JsonInclude
import com.google.gson.JsonObject
import net.nostalogic.datamodel.NoDate
import net.nostalogic.entities.EntityStatus
import net.nostalogic.users.datamodel.memberships.Membership

@JsonInclude(JsonInclude.Include.NON_NULL)
class User(
        val id: String? = null,
        var username: String? = null,
        var email: String? = null,
        var status: EntityStatus = EntityStatus.ACTIVE,
        var details: JsonObject? = null,
        var memberships: List<Membership>? = null,
        var created: NoDate? = null
)
