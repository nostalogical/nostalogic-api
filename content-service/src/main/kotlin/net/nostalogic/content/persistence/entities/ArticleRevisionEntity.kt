package net.nostalogic.content.persistence.entities

import net.nostalogic.persistence.entities.AbstractCoreEntity
import javax.persistence.Entity

@Entity(name = "article_revision")
class ArticleRevisionEntity(
    var name: String,
    var contents: String,
    val articleId: String,
    var committed: Boolean = false,
    var discarded: Boolean = false,
    creatorId: String
): AbstractCoreEntity(creatorId = creatorId)
