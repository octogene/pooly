package dev.octogene.pooly.ptgraph.api

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.right
import arrow.resilience.Schedule
import arrow.resilience.retry
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.exception.ApolloException
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.ptgraph.api.model.GraphDraw
import dev.octogene.pooly.thegraph.DrawsByAddressesQuery
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class PoolTogetherGraphQLClient(
    chainNetworks: List<ChainNetwork> = ChainNetwork.entries,
    builder: ApolloClient.Builder = ApolloClient.Builder(),
    private val logger: Logger = LoggerFactory.getLogger(PoolTogetherGraphQLClient::class.java)
) {
    private val backoff = Schedule
        .exponential<Throwable>(100.milliseconds, 2.0)
        .jittered(0.1)
    private val clients = chainNetworks.associateWith { network ->
        with(builder) {
            val id = if (network == ChainNetwork.BASE) 41211 else 63100
            serverUrl("https://api.studio.thegraph.com/query/$id/pt-v5-${network.name.lowercase()}/version/latest")
        }.build()
    }

    suspend fun getDraws(
        client: ApolloClient,
        addresses: List<String>,
        skip: Int,
        after: Long?
    ): Either<GraphQLServiceError, List<GraphDraw>> = either {
        val afterTimestamp = Optional.presentIfNotNull(after?.toString())
        logger.debug(
            "Fetching draws for {} addresses with skip {} and after {}",
            addresses.size,
            skip,
            after
        )
        backoff.retry {
            client.query(DrawsByAddressesQuery(addresses, skip, afterTimestamp)).execute()
        }.toEither().map { response ->
            when (response) {
                is GraphQLResponse.Success -> response.data.prizeClaims
                is GraphQLResponse.PartialSuccess -> {
                    logger.error("Partial success: {}", response.errors)
                    response.data.prizeClaims
                }
            }.mapNotNull { prizeClaim -> prizeClaim.toGraphDraw() }
        }.onLeft { error ->
            logger.error("Error fetching draws: {}", error)
        }.bind()
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getAllDraws(
        addresses: List<String>,
        chainNetwork: ChainNetwork,
        after: Long? = null
    ): List<GraphDraw> {
        logger.info("Fetching draws for ${addresses.size} addresses on $chainNetwork")
        val client = clients.getValue(chainNetwork)
        return buildList {
            var skip = 0
            while (true) {
                val draws = getDraws(client, addresses, skip, after).getOrNull()
                logger.debug("Found ${draws?.size} draws")
                if (draws.isNullOrEmpty()) {
                    break
                }
                addAll(draws)
                skip += draws.size
            }
        }
    }

    private fun DrawsByAddressesQuery.PrizeClaim.toGraphDraw() = timestamp.toLocalDateTime()?.let {
        GraphDraw(
            id = draw.drawId,
            payout = BigInteger(payout),
            // TODO: Convert to Instant, need to make sure of the initial format (epoch ?)
            timestamp = it,
            winner = winner,
            vault = prizeVault.id,
            transactionHash = draw.txHash
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun String.toLocalDateTime(): LocalDateTime? {
        return this.toLongOrNull()?.let {
            Instant.fromEpochSeconds(it)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }

    fun <T : Operation.Data> ApolloResponse<T>.toEither(): Either<GraphQLServiceError, GraphQLResponse<T>> =
        either {
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
                GraphQLServiceError.RuntimeError(scopedException).left()
            } else {
                GraphQLServiceError.UnknownError(
                    IllegalStateException("Apollo response is missing data, errors and exception")
                ).left()
            }
        }

    sealed class GraphQLResponse<T : Operation.Data> {
        data class Success<T : Operation.Data>(val data: T) : GraphQLResponse<T>()
        data class PartialSuccess<T : Operation.Data>(val data: T, val errors: List<Error>) :
            GraphQLResponse<T>()
    }

    sealed class GraphQLServiceError {
        data class RuntimeError(val error: ApolloException) : GraphQLServiceError()
        data class GraphQLErrors(val errors: List<Error>) : GraphQLServiceError()
        data class UnknownError(val error: Throwable) : GraphQLServiceError()
    }
}
