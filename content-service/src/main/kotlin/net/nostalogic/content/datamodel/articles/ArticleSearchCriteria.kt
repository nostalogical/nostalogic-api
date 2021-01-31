package net.nostalogic.content.datamodel.articles

import net.nostalogic.datamodel.NoPageable
import net.nostalogic.datamodel.SearchCriteria
import net.nostalogic.entities.EntityStatus
import java.util.*

class ArticleSearchCriteria(articleIds: Collection<String>? = null,
                            name: Collection<String>? = null,
                            contents: Collection<String>? = null,
                            status: Collection<EntityStatus>? = null,
                            page: NoPageable<Article>? = null): SearchCriteria<Article> {

    companion object {
        val DEFAULT_SORT_FIELDS = arrayOf("created", "id")
    }

    val articleIds: Collection<String> = articleIds ?: Collections.emptySet()
    val name: Collection<String> = name ?: Collections.emptySet()
    val contents: Collection<String> = contents ?: Collections.emptySet()
    val status: Collection<EntityStatus> = status ?: setOf(EntityStatus.ACTIVE)
    val page: NoPageable<Article> = page ?: NoPageable(1, 20, *DEFAULT_SORT_FIELDS)
}
