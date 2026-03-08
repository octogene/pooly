package dev.octogene.pooly.ptgraph.api

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.context.either
import arrow.core.right
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.exception.ApolloNetworkException
import dev.octogene.pooly.ptgraph.api.model.GraphQLResponse
import dev.octogene.pooly.ptgraph.api.model.QueryError

fun <T : Operation.Data> ApolloResponse<T>.toEither(): Either<QueryError, GraphQLResponse<T>> = either {
    val scopedData = data
    val scopedErrors = errors
    val scopedException = exception
    return if (scopedData != null && scopedErrors == null) {
        GraphQLResponse.Success(scopedData).right()
    } else if (scopedData != null && scopedErrors != null) {
        GraphQLResponse.PartialSuccess(scopedData, scopedErrors).right()
    } else if (!scopedErrors.isNullOrEmpty()) {
        QueryError.GraphQLErrors(scopedErrors).left()
    } else if (scopedException != null) {
        scopedException.toEither()
    } else {
        QueryError.UnexpectedError(
            IllegalStateException("Apollo response is missing data, errors and exception"),
        ).left()
    }
}

private fun ApolloException.toEither(): Either<QueryError.RuntimeError, Nothing> = when (val scopedException = this) {
    is ApolloHttpException -> {
        QueryError.HttpError(
            scopedException.statusCode,
            scopedException.headers.associate { it.name to it.value },
            scopedException.message,
            scopedException.cause,
        ).left()
    }

    is ApolloNetworkException -> {
        QueryError.NetworkError(scopedException).left()
    }

    else -> {
        QueryError.GenericRuntimeError(scopedException).left()
    }
}
