package net.nostalogic.persistence.entities

import net.nostalogic.datamodel.NoDate
import javax.persistence.Entity

@Entity(name = "service_config")
class ConfigEntity(
    val name: String,
    val setting: String, created: NoDate
) : AbstractJpaPersistable<Long>() {
    val lastSet = created.getTimestamp()
}
