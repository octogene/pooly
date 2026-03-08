package dev.octogene.pooly.ptgraph.api.model

import com.apollographql.apollo.api.Error
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.ApolloNetworkException

sealed interface QueryError {
    sealed interface RuntimeError : QueryError
    data class GenericRuntimeError(val error: ApolloException) : RuntimeError
    data class NetworkError(val error: ApolloNetworkException) : RuntimeError
    data class HttpError(
        val statusCode: Int,
        val headers: Map<String, String>,
        val message: String?,
        val cause: Throwable?,
    ) : RuntimeError
    data class GraphQLErrors(val errors: List<Error>) : QueryError
    data class UnexpectedError(val error: Throwable) : QueryError
}
