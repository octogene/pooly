package dev.octogene.pooly.pooltogether

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import app.cash.sqldelight.paging3.QueryPagingSource
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.Amount
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import dev.octogene.pooly.pooltogether.db.DrawQueries
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

@Inject
class PrizesRepository(private val drawQueries: DrawQueries) {
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
            },
        )
        return pager.flow.map { pagingData ->
            pagingData.map {
                Prize(
                    payout = Amount.from(it.amount),
                    timestamp = Instant.fromEpochSeconds(it.timestamp),
                    winner = Address.unsafeFrom(it.walletAddress),
                    transactionHash = it.transactionHash,
                    vault = Vault(
                        name = it.name,
                        symbol = it.symbol,
                        address = Address.unsafeFrom(it.vaultAddress),
                        network = it.network,
                        // Narrowing is ok as decimals is always a very small number (e.g. 18)
                        decimals = it.decimals.toInt(),
                    ),
                )
            }
        }
    }
}
