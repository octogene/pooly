package dev.octogene.pooly.ptgraph.api

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.ptgraph.api.model.GraphDraw
import dev.octogene.pooly.thegraph.DrawsByAddressesQuery
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.math.BigInteger
import kotlin.collections.associateWith
import kotlin.text.lowercase
import kotlin.text.toLongOrNull
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class PoolTogetherGraphQLClient(
    chainNetworks: List<ChainNetwork> = ChainNetwork.entries,
    builder: ApolloClient.Builder = ApolloClient.Builder()
) {
    private val clients = chainNetworks.associateWith { network ->
        with(builder) {
            serverUrl("https://api.studio.thegraph.com/query/63100/pt-v5-${network.name.lowercase()}/version/latest")
        }.build()
    }

    suspend fun getDraws(
        client: ApolloClient,
        addresses: List<String>,
        skip: Int,
        after: Long?
    ): List<GraphDraw>? {
        val afterTimestamp = Optional.presentIfNotNull(after?.toString())
        val response = client.query(DrawsByAddressesQuery(addresses, skip, afterTimestamp)).execute()
        return response.data?.prizeClaims?.mapNotNull { it.toGraphDraw() }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getAllDraws(
        addresses: List<String>,
        chainNetwork: ChainNetwork,
        after: Long? = null
    ): List<GraphDraw> {
        val client = clients[chainNetwork]
            ?: throw IllegalArgumentException("Unknown network: $chainNetwork")
        return buildList {
            var skip = 0
            while (true) {
                val draws = getDraws(client, addresses, skip, after)
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
            timestamp = it,
            winner = winner,
            vault = prizeVault.id
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
