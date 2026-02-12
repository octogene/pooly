package dev.octogene.pooly.ptgraph.api

import arrow.core.Either
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.fx.coroutines.parMap
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.ptgraph.api.model.DrawResult
import dev.octogene.pooly.ptgraph.api.model.GraphDraw
import dev.octogene.pooly.ptgraph.api.model.GraphQLResponse
import dev.octogene.pooly.ptgraph.api.model.GraphQLServiceError
import dev.octogene.pooly.thegraph.DrawsByAddressesQuery
import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigInteger
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val MAX_CONCURRENCY = 3

class PoolTogetherGraphQLClient(
    chainNetworks: List<ChainNetwork> = ChainNetwork.entries,
    builder: ApolloClient.Builder = ApolloClient.Builder(),
    private val logger: Logger = LoggerFactory.getLogger(PoolTogetherGraphQLClient::class.java),
) {
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
        after: Long?,
    ): Either<GraphQLServiceError, List<GraphDraw>> = either {
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
                if (error is GraphQLServiceError.HttpError) {
                    handleHttpErrorException(error)
                } else {
                    logger.error("Error fetching draws: {}", error)
                }
            }.bind()
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getAllDraws(addresses: List<String>, chainNetwork: ChainNetwork, after: Long? = null): List<GraphDraw> {
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

    @OptIn(ExperimentalTime::class)
    suspend fun getAllDraws(
        addresses: List<String>,
        after: Long? = null,
        chainNetworks: List<ChainNetwork>,
    ): DrawResult = chainNetworks.parMap(concurrency = MAX_CONCURRENCY) { chainNetwork ->
        val draws = getAllDraws(addresses, chainNetwork, after)
        chainNetwork to draws
    }.toMap()

    private fun DrawsByAddressesQuery.PrizeClaim.toGraphDraw(): GraphDraw? {
        val instant = timestamp.toInstant()
        if (instant == null) {
            logger.error("Failed to map prize claim {} to graph draw", this)
        }

        return instant?.let {
            GraphDraw(
                id = draw.drawId,
                payout = BigInteger(payout),
                timestamp = it,
                winner = winner,
                vault = prizeVault.id,
                transactionHash = draw.txHash,
            )
        }
    }

    private suspend fun handleHttpErrorException(error: GraphQLServiceError.HttpError) {
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

    @OptIn(ExperimentalTime::class)
    private fun String.toInstant(): Instant? = this.toLongOrNull()?.let { Instant.fromEpochSeconds(it) }
}
