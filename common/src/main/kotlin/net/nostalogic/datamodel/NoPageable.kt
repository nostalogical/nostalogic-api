package net.nostalogic.datamodel

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import kotlin.math.max
import kotlin.math.min

/**
 * Standard pagination object. Page is the offset in the number of entries, beginning at page 1.
 * E.g. size = 30, page = 2 returns the final 30 of the first 60 records.
 */
class NoPageable<T>(private val page: Int, private val size: Int, private vararg val props: String) {

    var hasNext: Boolean? = null

    private fun normalisedPage(): Int {
        return max(1, this.page)
    }

    private fun normalisedSize(): Int {
        return min(max(1, this.size), 1000)
    }

    /**
     * Spring pagination begins at page 0, this is shifted to begin from page 1 for better human readability.
     * Size of the page is confined to be within 1 and 1000
     */
    fun toQuery(): PageRequest {
        return PageRequest.of(
                normalisedPage() - 1,
                normalisedSize(),
                Sort.by(Sort.Direction.ASC, *this.props))
    }

    fun toResponse(content: ArrayList<T>): NoPageResponse<T> {
        return NoPageResponse(normalisedPage(), content.size, hasNext, content)
    }
}
