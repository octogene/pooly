package dev.octogene.pooly.common.db.repository

sealed class RepositoryError {
    abstract val message: String

    data class DatabaseError(override val message: String) : RepositoryError()
    data class NotFound(val entity: String, val identifier: String) : RepositoryError() {
        override val message: String
            get() = "Entity $entity with identifier $identifier not found"
    }
    data class AlreadyExists(override val message: String) : RepositoryError()
}
