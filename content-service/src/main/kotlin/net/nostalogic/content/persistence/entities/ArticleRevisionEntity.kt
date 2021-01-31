package net.nostalogic.content.persistence.entities

import net.nostalogic.entities.EntityStatus
import net.nostalogic.persistence.entities.AbstractCoreEntity
import java.sql.Timestamp
import java.time.Instant
import javax.persistence.Entity

@Entity(name = "article_revision")
class ArticleRevisionEntity(
    var name: String,
    var contents: String,
    val articleId: String,
    var lastUpdated: Timestamp = Timestamp.from(Instant.now()),
    var status: EntityStatus = EntityStatus.INACTIVE,
    creatorId: String
): AbstractCoreEntity(creatorId = creatorId)
