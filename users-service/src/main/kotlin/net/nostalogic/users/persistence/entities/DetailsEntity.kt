package net.nostalogic.users.persistence.entities

import net.nostalogic.entities.NoEntity
import net.nostalogic.persistence.entities.AbstractCoreEntity
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity(name = "details")
class DetailsEntity(
    id: String,
    creatorId: String,
    @Enumerated(value = EnumType.STRING)
    var entity: NoEntity,
    var details: String
): AbstractCoreEntity(id = id, creatorId = creatorId)
