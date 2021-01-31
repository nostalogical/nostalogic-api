package net.nostalogic.content.persistence.entities

import net.nostalogic.entities.EntityStatus
import net.nostalogic.persistence.entities.AbstractCoreEntity
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity(name = "navigation")
class NavEntity(
    var urn: String,
    var fullUrn: String,
    var parentId: String? = null,
    var icon: String,
    var text: String,
    val system: Boolean = false,
    @Enumerated(value = EnumType.STRING) var status: EntityStatus = EntityStatus.INACTIVE,
    creatorId: String
): AbstractCoreEntity(creatorId = creatorId)
