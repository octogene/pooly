package dev.octogene.pooly.pooltogether

import co.touchlab.kermit.Logger
import dev.octogene.pooly.pooltogether.db.DrawQueries
import dev.octogene.pooly.ptgraph.api.PoolTogetherGraphQLClient
import dev.octogene.pooly.ptgraph.api.model.GraphDraw
import dev.octogene.pooly.rpc.PoolTogetherRPCClient
import dev.octogene.pooly.rpc.RpcVault
import dev.octogene.pooly.shared.model.ChainNetwork
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Inject
class PoolTogetherClient(
    private val rpcClient: PoolTogetherRPCClient,
    private val graphClient: PoolTogetherGraphQLClient,
    private val drawQueries: DrawQueries
) {

    suspend fun getAllDraws(address: List<String>, network: ChainNetwork): List<GraphDraw> =
        withContext(Dispatchers.IO) {
            val latestDrawTimestamp = drawQueries.getLatestDrawTimestamp().executeAsOneOrNull()
            val draws = graphClient.getAllDraws(address, chainNetwork = network, latestDrawTimestamp)
            Logger.i("Fetched ${draws.size} draws from the graph.")
            draws
        }

    suspend fun getVaultsInfo(addresses: List<String>): List<RpcVault> {
        return rpcClient.getVaultInfoFromAdresses(addresses)
    }
}
