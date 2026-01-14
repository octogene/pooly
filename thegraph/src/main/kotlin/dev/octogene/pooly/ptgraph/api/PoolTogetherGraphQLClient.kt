package dev.octogene.pooly.ptgraph.api

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.ptgraph.api.model.GraphDraw
import dev.octogene.pooly.thegraph.DrawsByAddressesQuery
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigInteger
import kotlin.text.toLongOrNull
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class PoolTogetherGraphQLClient(
    chainNetworks: List<ChainNetwork> = ChainNetwork.entries,
    builder: ApolloClient.Builder = ApolloClient.Builder(),
    private val logger: Logger = LoggerFactory.getLogger(PoolTogetherGraphQLClient::class.java)
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
        after: Long?
    ): List<GraphDraw>? {
        val afterTimestamp = Optional.presentIfNotNull(after?.toString())
        logger.debug("Fetching draws for ${addresses.size} addresses with skip $skip and after $after")
        val response = client.query(DrawsByAddressesQuery(addresses, skip, afterTimestamp)).execute()
        return response.data?.prizeClaims?.mapNotNull { it.toGraphDraw() }
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
                val draws = getDraws(client, addresses, skip, after)
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
}
