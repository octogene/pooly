package dev.octogene.pooly.pooltogether

import androidx.paging.PagingData
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.map
import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.paging3.QueryPagingSource
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import dev.octogene.pooly.pooltogether.db.DrawQueries
import dev.octogene.pooly.pooltogether.db.GetAllDraws
import dev.octogene.pooly.pooltogether.db.VaultQueries
import dev.octogene.pooly.settings.db.WalletQueries
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigInteger
import kotlin.time.Instant

@Inject
class DrawsRepository(
    private val drawQueries: DrawQueries,
    private val walletQueries: WalletQueries,
    private val vaultQueries: VaultQueries,
    private val client: PoolyApiClientImpl
) {

    suspend fun updateDraws() {
        val wallets = walletQueries.getAllWalletsAdresses().executeAsList()
        val draws = client.getAllDraws(wallets, ChainNetwork.BASE)
        val vaults = vaultQueries.getAllVaultsAdresses().executeAsList().associateBy { it.network }
        val unknownVaults = draws.filter { !vaults.contains(ChainNetwork.BASE) }
    }

    suspend fun updatVaults(adresses: List<String>) {
        val vaults = client.getVaultsInfo(adresses)
    }

    fun getLatestDraw(address: String) {
        drawQueries.getLatestDrawForWallet(address)
    }

    fun getAllDraws(): Flow<Query<GetAllDraws>> {
        return drawQueries.getAllDraws().asFlow()
    }

    fun getAllDrawsPaged(): Flow<PagingData<Prize>> {
        val pager = Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true),
            pagingSourceFactory = {
                QueryPagingSource(
                    countQuery = drawQueries.countAllDraws(),
                    transacter = drawQueries,
                    context = Dispatchers.IO,
                    queryProvider = drawQueries::getAllDrawsPaged,
                )
            }
        )
        return pager.flow.map { pagingData ->
            pagingData.map {
                Prize(
                    payout = BigInteger(it.amount),
                    timestamp = Instant.fromEpochSeconds(it.timestamp),
                    winner = Address.unsafeFrom(it.walletAddress),
                    transactionHash = it.transactionHash,
                    vault = Vault(
                        name = it.name,
                        symbol = it.symbol,
                        address = Address.unsafeFrom(it.vaultAddress),
                        network = it.network,
                        // TODO: Add decimals to db
                        decimals = 18
                    )
                )
            }
        }
    }
}
