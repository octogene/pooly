package dev.octogene.pooly.server.model

import arrow.raise.ktor.server.response.Response
import arrow.raise.ktor.server.response.Response.Companion.Response
import dev.octogene.pooly.common.db.repository.RepositoryError
import dev.octogene.pooly.core.DomainError
import dev.octogene.pooly.core.InvalidField
import io.ktor.http.HttpStatusCode

fun mapToResponse(error: DomainError): Response {
    return when(error) {
        is InvalidField -> Response(HttpStatusCode.BadRequest, error.message)
    }
}

fun mapToResponse(error: RepositoryError): Response {
    return when(error) {
        is RepositoryError.DatabaseError -> Response(HttpStatusCode.InternalServerError, error.message)
        is RepositoryError.NotFound -> Response(HttpStatusCode.NotFound, error.entity)
    }
}
