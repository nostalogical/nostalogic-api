package net.nostalogic.persistence.entities

import net.nostalogic.datamodel.NoDate
import javax.persistence.Entity

@Entity(name = "entity_history")
class EntityModEntity(val userId: String, val entityId: String, val altEntityId: String?,
                      val original: String?, val updated: String?, val clusterId: String?,
                      created: NoDate) : AbstractJpaPersistable<Long>()
