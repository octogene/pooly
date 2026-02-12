package dev.octogene.pooly.pooltogether

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import co.touchlab.kermit.Logger
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import dev.octogene.pooly.pooltogether.db.DrawQueries
import dev.octogene.pooly.pooltogether.db.VaultQueries
import dev.octogene.pooly.settings.db.WalletQueries
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigInteger
import kotlin.time.Instant

@Inject
class PoolTogetherRepository(
    private val client: PoolyApiClient,
    private val walletQueries: WalletQueries,
    private val vaultQueries: VaultQueries,
    private val drawQueries: DrawQueries,
) {

    fun getAllDraws(): Flow<List<Prize>> = drawQueries.getAllDraws()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { allDraws ->
            Logger.i { "Mapping ${allDraws.size} draws to Prize objects" }
            allDraws.map { draws ->
                Prize(
                    payout = BigInteger(draws.amount),
                    timestamp = Instant.fromEpochSeconds(draws.timestamp),
                    winner = Address.unsafeFrom(draws.walletAddress),
                    transactionHash = draws.transactionHash,
                    vault = Vault(
                        address = Address.unsafeFrom(draws.vaultAddress),
                        name = draws.name,
                        symbol = draws.symbol,
                        decimals = 18,
                        network = draws.network,
                    ),
                )
            }
        }

    suspend fun updateAllVaults() {
        val wallets = walletQueries.getAllWallets().executeAsList().map { it.address }
        val draws = client.getAllDraws(wallets, ChainNetwork.BASE)
        drawQueries.transaction {
            afterRollback { Logger.e("No draws were inserted.") }
            afterCommit { Logger.i("${draws.size} draws were inserted.") }

            draws.map { it.vault }.distinctBy { it.address }.forEach { vault ->
                vaultQueries.insertVault(
                    vault.address.value,
                    vault.name,
                    vault.symbol,
                    vault.network,
                    vault.decimals.toLong(),
                )
            }

            draws.forEach {
                drawQueries.insertDraw(
                    it.winner.value,
                    it.vault.address.value,
                    it.payout.toString(),
                    it.transactionHash,
                )
            }
        }
    }
}
