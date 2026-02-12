package dev.octogene.pooly.ptgraph.api.model

import com.apollographql.apollo.api.Error
import com.apollographql.apollo.exception.ApolloException

sealed interface GraphQLServiceError {
    sealed interface RuntimeError : GraphQLServiceError
    data class GenericRuntimeError(val error: ApolloException) : RuntimeError
    data class HttpError(
        val statusCode: Int,
        val headers: Map<String, String>,
        val message: String?,
        val cause: Throwable?,
    ) : RuntimeError
    data class GraphQLErrors(val errors: List<Error>) : GraphQLServiceError
    data class UnexpectedError(val error: Throwable) : GraphQLServiceError
}
