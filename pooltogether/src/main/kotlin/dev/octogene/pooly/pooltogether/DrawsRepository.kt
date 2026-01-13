package dev.octogene.pooly.pooltogether

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.pooltogether.db.Database
import dev.octogene.pooly.pooltogether.db.GetAllDraws
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@Inject
class DrawsRepository(
    private val database: Database,
    private val client: PoolyApiClientImpl
) {

    suspend fun updateDraws() {
        val wallets = database.walletQueries.getAllWalletsAdresses().executeAsList()
        val draws = client.getAllDraws(wallets, ChainNetwork.BASE)
        val vaults = database.vaultQueries.getAllVaultsAdresses().executeAsList().associateBy { it.network }
        val unknownVaults = draws.filter { !vaults.contains(ChainNetwork.BASE) }
    }

    suspend fun updatVaults(adresses: List<String>) {
        val vaults = client.getVaultsInfo(adresses)
    }

    fun getLatestDraw(address: String) {
        database.drawQueries.getLatestDrawForWallet(address)
    }

    fun getAllDraws(): Flow<Query<GetAllDraws>> {
        return database.drawQueries.getAllDraws().asFlow()
    }
}
