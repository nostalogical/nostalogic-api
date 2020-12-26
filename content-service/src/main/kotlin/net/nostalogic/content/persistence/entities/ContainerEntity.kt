package net.nostalogic.content.persistence.entities

import net.nostalogic.constants.NoLocale
import net.nostalogic.content.datamodel.ContainerType
import net.nostalogic.persistence.entities.AbstractCoreEntity
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated


@Entity(name = "container")
class ContainerEntity(
    val navigationId: String,
    @Enumerated(EnumType.STRING) val type: ContainerType,
    val resourceId: String,
    @Enumerated(EnumType.STRING) var locale: NoLocale,
    creatorId: String
): AbstractCoreEntity(creatorId = creatorId)
