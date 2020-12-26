package net.nostalogic.content.persistence.entities

import net.nostalogic.content.datamodel.NavType
import net.nostalogic.entities.EntityStatus
import net.nostalogic.persistence.entities.AbstractCoreEntity
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity(name = "navigation")
class NavEntity(
    var urn: String,
    var fullUrn: String,
    var icon: String,
    var text: String,
    var ordinal: Int = 0,
    var parentId: String? = null,
    @Enumerated(value = EnumType.STRING) var type: NavType? = null,
    @Enumerated(value = EnumType.STRING) var status: EntityStatus = EntityStatus.INACTIVE,
    creatorId: String
): AbstractCoreEntity(creatorId = creatorId)
