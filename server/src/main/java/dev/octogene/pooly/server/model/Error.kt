package dev.octogene.pooly.server.model

import arrow.raise.ktor.server.response.Response
import arrow.raise.ktor.server.response.Response.Companion.Response
import dev.octogene.pooly.core.DomainError
import dev.octogene.pooly.core.InvalidField
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.postgresql.util.PSQLException

sealed interface PoolyError

sealed interface ClientError : PoolyError {
    val message: String
    data class BadRequest(override val message: String) : ClientError
}

sealed interface DatabaseError : PoolyError {
    val message: String
    data class GeneralError(override val message: String, val cause: Throwable? = null) : DatabaseError

    sealed interface OperationError : DatabaseError {
        data class NotFound(val entityType: String, val id: Any) : OperationError {
            override val message: String
                get() = "$entityType with ID $id not found."
        }

        sealed interface ConstraintViolation : OperationError {
            val constraintName: String?
            override val message: String
                get() = "Constraint violation: ${constraintName ?: "Unnamed Constraint"} failed."

            data class UniqueConstraint(
                override val constraintName: String?,
                val columnName: String,
                val value: Any
            ) : ConstraintViolation {
                override val message: String
                    get() = "Unique constraint violation on column '$columnName' with value '$value'. ${constraintName?.let { "Constraint: $it" } ?: ""}"
            }
        }
    }

    sealed interface TransactionError : DatabaseError {
        data class Rollback(val cause: Throwable? = null) : TransactionError {
            override val message: String
                get() = "Database transaction was rolled back. ${cause?.message ?: ""}"
        }
    }

    companion object {
        fun fromExposedException(e: Throwable): DatabaseError {
            return when (e) {
                is ExposedSQLException -> {
                    val cause = e.cause
                    if (cause is PSQLException && cause.message?.contains("violates unique constraint") == true) {
                        // TODO: Extract constraint name from message
                        OperationError.ConstraintViolation.UniqueConstraint(
                            "",
                            "",
                            ""
                        )
                    } else {
                        GeneralError("SQL Error: ${e.message}", e)
                    }
                }
                else -> {
                    GeneralError("An unexpected database error occurred: ${e.message}", e)
                }
            }
        }

        fun general(message: String, cause: Throwable? = null): DatabaseError {
            return GeneralError(message, cause)
        }
    }
}


fun mapToResponse(error: PoolyError): Response {
    return when(error) {
        is DatabaseError.OperationError.ConstraintViolation.UniqueConstraint ->
            Response(HttpStatusCode.BadRequest, error.message)
        is ClientError.BadRequest -> Response(HttpStatusCode.BadRequest, error.message)
        is DatabaseError.OperationError.NotFound -> Response(HttpStatusCode.NotFound, error.message)
        is DatabaseError.GeneralError ->
            Response(HttpStatusCode.InternalServerError, error.message)
        is DatabaseError.TransactionError.Rollback -> Response(HttpStatusCode.InternalServerError, error.message)
    }
}

fun mapToResponse(error: DomainError): Response {
    return when(error) {
        is InvalidField -> Response(HttpStatusCode.BadRequest, error.message)
    }
}
