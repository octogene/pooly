package dev.octogene.pooly.server.model

import arrow.raise.ktor.server.response.Response
import arrow.raise.ktor.server.response.Response.Companion.Response
import dev.octogene.pooly.common.db.repository.RepositoryError
import dev.octogene.pooly.core.DomainError
import dev.octogene.pooly.core.InvalidField
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ContentTransformationException
import kotlinx.serialization.Serializable
import org.slf4j.Logger

@Serializable
data class ApiError(val message: String, val code: Int)

fun mapToResponse(error: DomainError): Response = when (error) {
    is InvalidField -> apiErrorResponseOf(
        HttpStatusCode.BadRequest,
        error.message,
    )
}

fun mapToResponse(error: RepositoryError, logger: Logger? = null): Response = when (error) {
    is RepositoryError.DatabaseError -> {
        logger?.error("Database error: {}", error.message)
        apiErrorResponseOf(
            InternalServerError,
            "Internal server error",
        )
    }

    is RepositoryError.NotFound -> apiErrorResponseOf(
        HttpStatusCode.NotFound,
        "Unable to find ${error.identifier}",
    )

    is RepositoryError.AlreadyExists -> apiErrorResponseOf(
        HttpStatusCode.BadRequest,
        "Failed to create resource, it already exists.",
    )
}

fun mapToResponse(throwable: Throwable, message: String): Response = when (throwable) {
    is ContentTransformationException, is BadRequestException -> apiErrorResponseOf(
        HttpStatusCode.BadRequest,
        message,
    )

    else -> Response(InternalServerError)
}

fun mapToResponse(error: ApplicationError.AuthenticationError): Response = when (error) {
    is ApplicationError.AuthenticationError.InternalError -> apiErrorResponseOf(
        InternalServerError,
        message = "Server failure, please retry later",
    )

    ApplicationError.AuthenticationError.InvalidCredentials,
    is ApplicationError.AuthenticationError.UserNotFound,
    -> apiErrorResponseOf(
        Unauthorized,
        "Invalid credentials",
    )

    is ApplicationError.AuthenticationError.UserAlreadyExists -> apiErrorResponseOf(
        BadRequest,
        "User already exists",
    )
}

fun apiErrorResponseOf(status: HttpStatusCode, message: String) = Response(status, ApiError(message, status.value))
