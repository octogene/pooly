package dev.octogene.pooly.pooltogether

import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import dev.octogene.pooly.pooltogether.db.DrawQueries
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Inject
class PoolyApiClientImpl(
    private val drawQueries: DrawQueries
) : PoolyApiClient {

    override suspend fun getAllDraws(address: List<String>, network: ChainNetwork): List<Prize> =
        withContext(Dispatchers.IO) {
            val latestDrawTimestamp = drawQueries.getLatestDrawTimestamp().executeAsOneOrNull()
            emptyList()
        }

    override suspend fun getVaultsInfo(addresses: List<String>): List<Vault> {
        return emptyList()
    }
}

interface PoolyApiClient {
    suspend fun getAllDraws(address: List<String>, network: ChainNetwork): List<Prize>
    suspend fun getVaultsInfo(addresses: List<String>): List<Vault>
}
