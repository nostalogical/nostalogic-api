package net.nostalogic.persistence.entities

import net.nostalogic.datamodel.NoDate
import javax.persistence.Entity

@Entity(name = "schema_history")
class SchemaModEntity(val name: String, val setting: String, created: NoDate) : AbstractJpaPersistable<Long>() {

    // timestamp
    // type (startup, migration, rollback)
    // rollback timestamp
    // version number (yyyy-mm-dd-patch)

}
