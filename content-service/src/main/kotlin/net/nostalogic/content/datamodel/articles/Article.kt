package net.nostalogic.content.datamodel.articles

import com.fasterxml.jackson.annotation.JsonInclude
import net.nostalogic.content.datamodel.containers.Container
import net.nostalogic.datamodel.NoDate
import net.nostalogic.entities.EntityStatus

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Article(
    var id: String?,
    var revision: String? = null,
    var title: String?,
    var body: String?,
    var status: EntityStatus? = null,
    var revisionStatus: EntityStatus? = null,
    var created: NoDate?,
    var creatorId: String?,
    var lastUpdated: NoDate? = null,
    var lastUpdaterId: String? = null,
    var containers: List<Container>? = null
)
