package net.nostalogic.content.persistence.entities

import net.nostalogic.entities.EntityStatus
import net.nostalogic.persistence.entities.AbstractCoreEntity
import net.nostalogic.utils.EntityUtils
import java.sql.Timestamp
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity(name = "article")
class ArticleEntity(
    var name: String,
    var contents: String,
    @Enumerated(EnumType.STRING)
    var status: EntityStatus = EntityStatus.INACTIVE,
    var lastUpdated: Timestamp = Timestamp.from(Instant.now()),
    var lastUpdaterId: String = EntityUtils.SYSTEM_ID,
    creatorId: String
): AbstractCoreEntity(creatorId = creatorId)
