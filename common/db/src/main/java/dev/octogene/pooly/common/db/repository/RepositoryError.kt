package dev.octogene.pooly.common.db.repository

sealed class RepositoryError {
    data class DatabaseError(val message: String) : RepositoryError()
    data class NotFound(val entity: String, val identifier: String) : RepositoryError()
}
