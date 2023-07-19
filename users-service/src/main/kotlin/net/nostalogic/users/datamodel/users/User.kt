package net.nostalogic.users.datamodel.users

import com.fasterxml.jackson.annotation.JsonInclude
import net.nostalogic.datamodel.NoDate
import net.nostalogic.datamodel.NoPageResponse
import net.nostalogic.datamodel.access.EntityRights
import net.nostalogic.entities.EntityStatus
import net.nostalogic.entities.NoEntity
import net.nostalogic.users.datamodel.memberships.Membership

@JsonInclude(JsonInclude.Include.NON_NULL)
class User(
        val id: String? = null,
        var username: String? = null,
        var displayName: String? = null,
        var email: String? = null,
        var status: EntityStatus? = EntityStatus.ACTIVE,
        var details: Any? = null,
        var locale: String? = null,
        var memberships: NoPageResponse<Membership>? = null,
        var rights: Map<NoEntity, EntityRights>? = null,
        var created: NoDate? = null
)
