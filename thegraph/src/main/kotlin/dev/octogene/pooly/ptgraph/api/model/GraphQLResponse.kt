package dev.octogene.pooly.ptgraph.api.model

import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.Operation

sealed class GraphQLResponse<T : Operation.Data> {
    data class Success<T : Operation.Data>(val data: T) : GraphQLResponse<T>()
    data class PartialSuccess<T : Operation.Data>(val data: T, val errors: List<Error>) :
        GraphQLResponse<T>()
}