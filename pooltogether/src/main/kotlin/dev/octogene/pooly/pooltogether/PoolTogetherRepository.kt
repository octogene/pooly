package dev.octogene.pooly.pooltogether

import co.touchlab.kermit.Logger
import dev.octogene.pooly.pooltogether.db.DrawQueries
import dev.octogene.pooly.settings.db.WalletQueries
import dev.octogene.pooly.shared.model.ChainNetwork
import dev.zacsweers.metro.Inject

@Inject
class PoolTogetherRepository(
    private val client: PoolTogetherClient,
    private val walletQueries: WalletQueries,
    private val drawQueries: DrawQueries
) {
    suspend fun updateAllVaults() {
        val wallets = walletQueries.getAllWallets().executeAsList().map { it.address }
        val draws = client.getAllDraws(wallets, ChainNetwork.BASE)
        drawQueries.transaction {
            afterRollback { Logger.e("No draws were inserted.") }
            afterCommit { Logger.i("${draws.size} draws were inserted.") }

            draws.forEach {
                drawQueries.insertDraw(it.winner, it.vault, it.payout.toString())
            }
        }
    }
}
