package dev.octogene.pooly.common.db.repository

import arrow.core.raise.context.ensure
import arrow.core.raise.either
import kotlinx.serialization.Serializable
import kotlin.math.ceil

data class PageRequest(val page: Int, val pageSize: Int) {
    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
        private const val DEFAULT_MIN_PAGE = 1
        private const val DEFAULT_MAX_PAGE_SIZE = 1
        fun create(page: Int, pageSize: Int?) = either {
            val currentPageSize = pageSize ?: DEFAULT_PAGE_SIZE
            ensure(page >= DEFAULT_MIN_PAGE) { "Page must be >= 1" }
            ensure(currentPageSize >= DEFAULT_MIN_PAGE) { "Page size must be >= 1" }
            ensure(currentPageSize <= DEFAULT_MAX_PAGE_SIZE) { "Page size must be <= 1000" }
            PageRequest(page, currentPageSize)
        }
    }

    val offset: Long
        get() = ((page - 1) * pageSize).toLong()
}

@Serializable
data class Page<T>(val items: List<T>, val totalCount: Long, val page: Int, val pageSize: Int) {
    val totalPages: Int = ceil(totalCount.toDouble() / pageSize).toInt()
    val hasNext: Boolean = page < totalPages
    val hasPrevious: Boolean = page > 1
}
