package dev.octogene.pooly.common.db.repository

import arrow.core.raise.context.ensure
import arrow.core.raise.either
import kotlinx.serialization.Serializable
import kotlin.math.ceil

data class PageRequest(
    val page: Int,
    val pageSize: Int
) {
    companion object {
        fun create(page: Int = 1, pageSize: Int? = 20) = either {
            val pageSize = pageSize ?: 20
            ensure(page >= 1) { "Page must be >= 1" }
            ensure(pageSize >= 1) { "Page size must be >= 1" }
            ensure(pageSize <= 100) { "Page size must be <= 1000" }
            PageRequest(page, pageSize)
        }
    }

    val offset: Long
        get() = ((page - 1) * pageSize).toLong()
}

@Serializable
data class Page<T>(
    val items: List<T>,
    val totalCount: Long,
    val page: Int,
    val pageSize: Int
) {
    val totalPages: Int = ceil(totalCount.toDouble() / pageSize).toInt()
    val hasNext: Boolean = page < totalPages
    val hasPrevious: Boolean = page > 1
}
