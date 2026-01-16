package dev.octogene.pooly.pooltogether

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import co.touchlab.kermit.Logger
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import dev.octogene.pooly.pooltogether.db.DrawQueries
import dev.octogene.pooly.settings.db.WalletQueries
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.math.BigInteger
import kotlin.time.Instant

@Inject
class PoolTogetherRepository(
    private val client: PoolyApiClient,
    private val walletQueries: WalletQueries,
    private val drawQueries: DrawQueries
) {

    // TODO: Needs debug
    fun getAllDraws(): Flow<List<Prize>> {
        return drawQueries.getAllDraws().asFlow().onEach {
            Logger.i { "Draws were updated" }
        }.mapToList(Dispatchers.IO).map { allDraws ->
            allDraws.map { draws ->
                Prize(
                    payout = BigInteger(draws.amount),
                    timestamp = Instant.fromEpochSeconds(draws.timestamp),
                    winner = Address.unsafeFrom(draws.walletAddress),
                    vault = Vault(
                        address = Address.unsafeFrom(draws.vaultAddress),
                        name = draws.name,
                        symbol = draws.symbol,
                        decimals = 18,
                        network = draws.network
                    )
                )
            }
        }
    }

    suspend fun updateAllVaults() {
        val wallets = walletQueries.getAllWallets().executeAsList().map { it.address }
        val draws = client.getAllDraws(wallets, ChainNetwork.BASE)
        drawQueries.transaction {
            afterRollback { Logger.e("No draws were inserted.") }
            afterCommit { Logger.i("${draws.size} draws were inserted.") }

            draws.forEach {
                drawQueries.insertDraw(
                    it.winner.value,
                    it.vault.address.value,
                    it.payout.toString()
                )
            }
        }
    }
}
