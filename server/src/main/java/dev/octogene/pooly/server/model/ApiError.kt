package dev.octogene.pooly.server.model

import arrow.raise.ktor.server.response.Response
import arrow.raise.ktor.server.response.Response.Companion.Response
import dev.octogene.pooly.common.db.repository.RepositoryError
import dev.octogene.pooly.core.DomainError
import dev.octogene.pooly.core.InvalidField
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ContentTransformationException
import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val message: String,
    val code: Int
)

fun mapToResponse(error: DomainError): Response {
    return when (error) {
        is InvalidField -> Response(
            HttpStatusCode.BadRequest,
            ApiError(error.message, HttpStatusCode.BadRequest.value)
        )
    }
}

fun mapToResponse(error: RepositoryError): Response {
    return when (error) {
        is RepositoryError.DatabaseError -> Response(
            HttpStatusCode.InternalServerError,
            ApiError(error.message, HttpStatusCode.InternalServerError.value)
        )

        is RepositoryError.NotFound -> Response(
            HttpStatusCode.NotFound,
            ApiError("Unable to find ${error.identifier}", HttpStatusCode.NotFound.value)
        )

        is RepositoryError.AlreadyExists -> Response(
            HttpStatusCode.BadRequest,
            ApiError("Failed to create resource, it already exists.", HttpStatusCode.BadRequest.value)
        )
    }
}

fun mapToResponse(throwable: Throwable, message: String): Response {
    return when (throwable) {
        is ContentTransformationException, is BadRequestException -> Response(
            HttpStatusCode.BadRequest,
            ApiError(message, HttpStatusCode.BadRequest.value)
        )
        else -> Response(HttpStatusCode.InternalServerError)
    }
}