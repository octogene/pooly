package dev.octogene.pooly.settings

import app.cash.sqldelight.coroutines.asFlow
import dev.octogene.pooly.settings.db.NetworkStateQueries
import dev.octogene.pooly.settings.db.WalletQueries
import dev.octogene.pooly.shared.model.ChainNetwork
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsRepository {
    fun setNetworks(networks: List<String>)

    fun toggleNetwork(network: ChainNetwork)

    fun observeNetworks(): Flow<List<ChainNetwork>>

    fun setRpcFor(network: ChainNetwork, rpc: String)

    fun addWallet(address: String, name: String? = null)

    fun observeWallets(): Flow<List<String>>
}

@Inject
@ContributesBinding(AppScope::class)
class SettingsRepositoryImpl(
    private val queries: NetworkStateQueries,
    private val walletQueries: WalletQueries
) : SettingsRepository {

    override fun setNetworks(networks: List<String>) {
        val db = queries.selectAllNetworks()
    }

    override fun toggleNetwork(network: ChainNetwork) {
        queries.toggleNetworkState(network)
    }

    override fun observeNetworks(): Flow<List<ChainNetwork>> =
        queries.selectAllNetworks().asFlow().map { queries ->
            queries.executeAsList().filter { it.isActive }.map { it.name }
        }

    override fun setRpcFor(network: ChainNetwork, rpc: String) {
        queries.setRpc(rpc, network)
    }

    override fun addWallet(address: String, name: String?) {
        walletQueries.addWallet(address, name)
    }

    override fun observeWallets(): Flow<List<String>> =
        walletQueries.getAllWalletsAdresses().asFlow().map { it.executeAsList() }
}
