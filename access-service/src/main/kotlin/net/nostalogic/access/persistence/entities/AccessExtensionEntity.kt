package net.nostalogic.access.persistence.entities

import net.nostalogic.constants.Tenant
import net.nostalogic.utils.EntityUtils
import java.sql.Timestamp
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.Id

@Entity(name = "access_extension")
class AccessExtensionEntity(
    val userId: String,
    val entityId: String,
    @Id val id: String = EntityUtils.uuid(),
    val created: Timestamp = Timestamp.from(Instant.now()),
    val tenant: String = Tenant.NOSTALOGIC.name.lowercase()
)
