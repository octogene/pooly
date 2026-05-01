package dev.octogene.pooly.ptgraph.api

import arrow.core.Either
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.fx.coroutines.parMap
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.ptgraph.api.model.DrawResult
import dev.octogene.pooly.ptgraph.api.model.GraphQLResponse
import dev.octogene.pooly.ptgraph.api.model.IndexedPrize
import dev.octogene.pooly.ptgraph.api.model.QueryError
import dev.octogene.pooly.thegraph.DrawsByAddressesQuery
import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val MAX_CONCURRENCY = 3
private const val BASE_GRAPH_ID = 41211
private const val DEFAULT_GRAPH_ID = 63100

class PoolTogetherGraphQLClient(
    chainNetworks: List<ChainNetwork> = ChainNetwork.entries,
    builder: ApolloClient.Builder = ApolloClient.Builder(),
    private val logger: Logger = LoggerFactory.getLogger(PoolTogetherGraphQLClient::class.java),
) {
    private val clients = chainNetworks.associateWith { network ->
        with(builder) {
            val id = if (network == ChainNetwork.BASE) BASE_GRAPH_ID else DEFAULT_GRAPH_ID
            serverUrl("https://api.studio.thegraph.com/query/$id/pt-v5-${network.name.lowercase()}/version/latest")
        }.build()
    }

    suspend fun getDraws(
        client: ApolloClient,
        addresses: List<String>,
        skip: Int,
        after: Long?,
    ): Either<QueryError, List<IndexedPrize>> = either {
        val afterTimestamp = Optional.presentIfNotNull(after?.toString())
        logger.debug(
            "Fetching draws for {} addresses with skip {} and after {}",
            addresses.size,
            skip,
            after,
        )
        client.query(DrawsByAddressesQuery(addresses, skip, afterTimestamp)).execute()
            .toEither().map { response ->
                when (response) {
                    is GraphQLResponse.Success -> response.data.prizeClaims

                    is GraphQLResponse.PartialSuccess -> {
                        logger.error("Partial success: {}", response.errors)
                        response.data.prizeClaims
                    }
                }.mapNotNull { prizeClaim -> prizeClaim.toGraphDraw() }
            }.onLeft { error ->
                handleQueryError(error)
            }.bind()
    }

    private suspend fun handleQueryError(error: QueryError) {
        when (error) {
            is QueryError.HttpError -> {
                handleHttpErrorException(error)
            }

            is QueryError.NetworkError -> {
                logger.error("Network error fetching draws: {}", error.error.cause ?: "No cause")
            }

            is QueryError.GraphQLErrors -> {
                logger.error("GraphQL error fetching draws: {}", error.errors)
            }

            else -> {
                logger.error("Error fetching draws: {}", error)
            }
        }
    }

    suspend fun getAllDraws(
        addresses: List<String>,
        chainNetwork: ChainNetwork,
        after: Long? = null,
    ): List<IndexedPrize> = buildList {
        accumulateAllDraws(addresses, chainNetwork, after) { draws ->
            addAll(draws)
        }
    }

    suspend fun getAllDrawsByVault(
        addresses: List<String>,
        chainNetwork: ChainNetwork,
        after: Long? = null,
    ): Map<String, List<IndexedPrize>> {
        val drawsByVault = mutableMapOf<String, MutableList<IndexedPrize>>()
        accumulateAllDraws(addresses, chainNetwork, after) { draws ->
            for (draw in draws) {
                drawsByVault[draw.vault] =
                    drawsByVault.getOrDefault(
                        key = draw.vault,
                        defaultValue = mutableListOf(),
                    ).apply { add(draw) }
            }
        }
        return drawsByVault
    }

    private suspend fun accumulateAllDraws(
        addresses: List<String>,
        chainNetwork: ChainNetwork,
        after: Long? = null,
        accumulate: (List<IndexedPrize>) -> Unit,
    ) {
        logger.info("Fetching draws for ${addresses.size} addresses on $chainNetwork")
        val client = clients.getValue(chainNetwork)
        var skip = 0
        while (true) {
            val draws = getDraws(client, addresses, skip, after).getOrNull()
            logger.debug("Found ${draws?.size} draws")
            if (draws.isNullOrEmpty()) {
                break
            }
            accumulate(draws)
            skip += draws.size
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getAllDraws(
        addresses: List<String>,
        after: Long? = null,
        chainNetworks: List<ChainNetwork>,
    ): DrawResult = chainNetworks.parMap(concurrency = MAX_CONCURRENCY) { chainNetwork ->
        val draws = getAllDraws(addresses, chainNetwork, after)
        chainNetwork to draws
    }.toMap()

    private suspend fun handleHttpErrorException(error: QueryError.HttpError) {
        val xRateLimitRemaining = error.headers["x-ratelimit-remaining"]?.toIntOrNull()
        val xRateLimitLimit = error.headers["x-ratelimit-limit"]?.toIntOrNull()
        val xRateLimitReset = error.headers["x-ratelimit-reset"]?.toLongOrNull()
            ?.let { Instant.fromEpochSeconds(it) }
        if (xRateLimitRemaining == 0) {
            logger.error(
                "Rate limit reached : {} / {} (resets on {})",
                xRateLimitRemaining,
                xRateLimitLimit,
                xRateLimitReset,
            )
            error.headers["retry-after"]?.toIntOrNull()?.seconds?.let { retryAfter ->
                logger.info("Retrying after {}", retryAfter)
                delay(retryAfter)
            }
        } else {
            logger.debug(
                "Rate limit status : {} / {}",
                xRateLimitRemaining,
                xRateLimitLimit,
            )
            logger.error("Error fetching draws headers: {}", error.message)
            logger.debug(
                "Request headers {}",
                error.headers
                    .map { (key, value) -> "$key : $value" }
                    .joinToString("\n"),
            )
        }
    }
}
