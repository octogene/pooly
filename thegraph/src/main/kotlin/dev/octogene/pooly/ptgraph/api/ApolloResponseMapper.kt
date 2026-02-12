package dev.octogene.pooly.ptgraph.api

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.context.either
import arrow.core.right
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.exception.ApolloHttpException
import dev.octogene.pooly.ptgraph.api.model.GraphQLResponse
import dev.octogene.pooly.ptgraph.api.model.GraphQLServiceError

fun <T : Operation.Data> ApolloResponse<T>.toEither(): Either<GraphQLServiceError, GraphQLResponse<T>> = either {
    val scopedData = data
    val scopedErrors = errors
    val scopedException = exception
    return if (scopedData != null && scopedErrors == null) {
        GraphQLResponse.Success(scopedData).right()
    } else if (scopedData != null && scopedErrors != null) {
        GraphQLResponse.PartialSuccess(scopedData, scopedErrors).right()
    } else if (!scopedErrors.isNullOrEmpty()) {
        GraphQLServiceError.GraphQLErrors(scopedErrors).left()
    } else if (scopedException != null) {
        if (scopedException is ApolloHttpException) {
            GraphQLServiceError.HttpError(
                scopedException.statusCode,
                scopedException.headers.associate { it.name to it.value },
                scopedException.message,
                scopedException.cause,
            ).left()
        } else {
            GraphQLServiceError.GenericRuntimeError(scopedException).left()
        }
    } else {
        GraphQLServiceError.UnexpectedError(
            IllegalStateException("Apollo response is missing data, errors and exception"),
        ).left()
    }
}
