package net.nostalogic.content.persistence.entities

import net.nostalogic.content.datamodel.navigations.NavType
import net.nostalogic.entities.EntityStatus
import net.nostalogic.persistence.entities.AbstractCoreEntity
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity(name = "navigation_link")
class NavLinkEntity(
    var parentId: String,
    var childId: String,
    var ordinal: Int = 0,
    @Enumerated(value = EnumType.STRING) var type: NavType,
    @Enumerated(value = EnumType.STRING) var status: EntityStatus = EntityStatus.INACTIVE,
    creatorId: String
): AbstractCoreEntity(creatorId = creatorId)
