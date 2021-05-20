package net.nostalogic.content.datamodel.articles

import com.fasterxml.jackson.annotation.JsonInclude
import net.nostalogic.content.datamodel.containers.Container
import net.nostalogic.datamodel.NoDate
import net.nostalogic.entities.EntityStatus

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Article(
    var id: String? = null,
    var revision: String? = null,
    var title: String? = null,
    var body: String? = null,
    var status: EntityStatus? = null,
    var revisionStatus: EntityStatus? = null,
    var created: NoDate? = null,
    var creatorId: String? = null,
    var lastUpdated: NoDate? = null,
    var lastUpdaterId: String? = null,
    var containers: List<Container>? = null
)
