package net.nostalogic.datamodel

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import kotlin.math.max
import kotlin.math.min

/**
 * Standard pagination object. Page is the offset in the number of entries, beginning at page 1.
 * E.g. size = 30, page = 2 returns the final 30 of the first 60 records.
 */
class NoPageable<T>(private val page: Int = 1, private val size: Int = MAX_PAGE_SIZE, private vararg val sortFields: String) {

    companion object {
        private const val MAX_PAGE_SIZE = 1_000
    }

    var hasNext: Boolean? = false

    private fun normalisedPage(): Int {
        return max(1, this.page)
    }

    private fun normalisedSize(): Int {
        return min(max(1, this.size), MAX_PAGE_SIZE)
    }

    /**
     * Spring pagination begins at page 0, this is shifted to begin from page 1 for better human readability.
     * Size of the page is confined to be within 1 and MAX_PAGE_SIZE
     */
    fun toQuery(): PageRequest {
        return PageRequest.of(
                normalisedPage() - 1,
                normalisedSize(),
                Sort.by(Sort.Direction.ASC, *this.sortFields))
    }

    fun toResponse(content: List<T>): NoPageResponse<T> {
        return NoPageResponse(normalisedPage(), content.size, hasNext, content)
    }
}
