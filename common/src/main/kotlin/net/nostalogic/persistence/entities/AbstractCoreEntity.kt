package net.nostalogic.persistence.entities

import net.nostalogic.constants.Tenant
import net.nostalogic.utils.EntityUtils
import java.sql.Timestamp
import java.time.Instant
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
open class AbstractCoreEntity(
        @Id val id: String = EntityUtils.uuid(),
        val created: Timestamp = Timestamp.from(Instant.now()),
        val creatorId: String = EntityUtils.SYSTEM_ID,
        val tenant: String = Tenant.NOSTALOGIC.name.toLowerCase()
)
