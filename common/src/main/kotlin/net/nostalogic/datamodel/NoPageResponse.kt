package net.nostalogic.datamodel

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class NoPageResponse<T>(
    val page: Int,
    val size: Int,
    val pageCount: Int,
    val totalSize: Long,
    val hasNext: Boolean?,
    val query: String?,
    val content: List<T>
)
