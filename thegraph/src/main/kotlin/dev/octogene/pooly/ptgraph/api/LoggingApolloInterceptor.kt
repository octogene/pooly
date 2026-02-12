package dev.octogene.pooly.ptgraph.api

import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.apollographql.apollo.network.http.HttpInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.slf4j.Logger

class LoggingApolloInterceptor(private val logger: Logger) : ApolloInterceptor {
    override fun <D : Operation.Data> intercept(
        request: ApolloRequest<D>,
        chain: ApolloInterceptorChain,
    ): Flow<ApolloResponse<D>> = chain.proceed(request).onEach { response ->
        (response.executionContext as? HttpInfo)?.let { context ->
            logger.debug(
                "Received HTTP response for ${request.operation.name()}: ${context.statusCode}\n ${
                    context.headers.joinToString(
                        "\n",
                    )
                }",
            )
        }
        logger.debug("Received response for ${request.operation.name()}: ${response.errors}")
    }
}
