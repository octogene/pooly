package dev.octogene.pooly.worker

import arrow.core.raise.context.bind
import arrow.core.raise.either
import dev.octogene.pooly.common.db.checkDatabaseInitialization
import dev.octogene.pooly.common.db.repository.PrizeRepository
import dev.octogene.pooly.common.db.repository.VaultRepository
import dev.octogene.pooly.common.db.repository.WalletRepository
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import dev.octogene.pooly.ptgraph.api.PoolTogetherGraphQLClient
import dev.octogene.pooly.ptgraph.api.model.GraphDraw
import dev.octogene.pooly.rpc.PoolTogetherRPCClient
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.jetbrains.exposed.v1.jdbc.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class Worker(
    private val rpcClient: PoolTogetherRPCClient,
    private val graphClient: PoolTogetherGraphQLClient,
    private val database: Database,
    private val vaultRepository: VaultRepository,
    private val prizeRepository: PrizeRepository,
    private val walletRepository: WalletRepository,
    private val checkInterval: Duration,
    private val logger: Logger = LoggerFactory.getLogger(Worker::class.java)
) {
    private var lastCheckTimeStamp: Instant? = null
    suspend fun run() = coroutineScope {
        checkDatabaseInitialization(database)
        logger.info("Starting worker")

        while (isActive) {
            val addresses = walletRepository.getAllWalletAddresses().getOrNull()
            if (addresses != null) {
                val (draws, newVaults) = fetchDrawsAndVaults(addresses)
                syncVaultsAndPrizes(newVaults, draws).onLeft {
                    logger.error("Error syncing vaults and prizes", it)
                }
            } else {
                logger.info("No wallets found in database")
            }
            lastCheckTimeStamp = Clock.System.now()
            delay(checkInterval)
        }
    }

    private suspend fun syncVaultsAndPrizes(
        newVaults: List<Vault>,
        draws: List<GraphDraw>
    ) = either {
        vaultRepository.insertVaults(newVaults, ChainNetwork.BASE).bind()
        val prizes = draws.groupBy { it.vault }.flatMap { (vault, draws) ->
            val vault = vaultRepository.getVaultFromAddress(vault).bind()
            draws.map { draw -> draw.toPrize(draw, vault) }
        }
        prizeRepository.insertPrizes(prizes).onLeft {
            logger.error("Error inserting prizes", it)
        }.bind()
    }

    private fun GraphDraw.toPrize(
        draw: GraphDraw,
        vault: Vault
    ): Prize = Prize(
        winner = Address.unsafeFrom(draw.winner),
        payout = draw.payout,
        transactionHash = draw.transactionHash,
        timestamp = draw.timestamp.toInstant(TimeZone.UTC),
        vault = vault
    )

    private suspend fun fetchDrawsAndVaults(addresses: List<Address>): Pair<List<GraphDraw>, List<Vault>> {
        logger.info("Getting draws for {} addresses", addresses.size)
        val draws = graphClient.getAllDraws(
            addresses = addresses.map { it.value },
            chainNetwork = ChainNetwork.BASE,
            after = null
        )
        logger.info("Found {} draws", draws.size)
        val unknownVaultsAddresses =
            vaultRepository.findUnknownVaults(draws.map { it.vault }.distinct()).getOrNull()
                ?: emptyList()
        logger.info("found {} unknown vaults", unknownVaultsAddresses.size)
        val newVaults = rpcClient.getVaultInfoFromAdresses(unknownVaultsAddresses)
        logger.info("found {} new vaults", newVaults.size)
        return Pair(draws, newVaults)
    }
}
