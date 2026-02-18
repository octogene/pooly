package dev.octogene.pooly.worker

import arrow.core.Either
import arrow.core.raise.either
import dev.octogene.pooly.common.db.repository.PrizeRepository
import dev.octogene.pooly.common.db.repository.RepositoryError
import dev.octogene.pooly.common.db.repository.VaultRepository
import dev.octogene.pooly.common.db.repository.WalletRepository
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.ptgraph.api.PoolTogetherGraphQLClient
import dev.octogene.pooly.ptgraph.api.model.GraphDraw
import dev.octogene.pooly.ptgraph.api.model.toPrize
import dev.octogene.pooly.rpc.PoolTogetherRPCClient
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class Worker(
    private val rpcClient: PoolTogetherRPCClient,
    private val graphClient: PoolTogetherGraphQLClient,
    private val vaultRepository: VaultRepository,
    private val prizeRepository: PrizeRepository,
    private val walletRepository: WalletRepository,
    private val checkInterval: Duration,
    private val logger: Logger = LoggerFactory.getLogger(Worker::class.java),
) {
    private var lastCheckTimeStamp: Instant? = null
    suspend fun run() = coroutineScope {
        logger.info("Starting worker")

        while (isActive) {
            val addresses = walletRepository.getAllWalletAddresses().onLeft {
                logger.error("Failed to retrieve wallet addresses : {}", it)
            }.getOrNull()
            if (addresses != null) {
                fetchAndSync(addresses).mapLeft {
                    logger.error("Failed to fetch and sync data : {}", it)
                }
            } else {
                logger.info("No wallets found in database")
            }
            delay(checkInterval)
        }
    }

    private suspend fun fetchAndSync(addresses: List<Address>): Either<RepositoryError, Unit> = either {
        logger.info("Getting draws for {} addresses", addresses.size)
        val drawsByVaultId = graphClient.getAllDrawsByVault(
            addresses = addresses.map { it.value },
            chainNetwork = ChainNetwork.BASE,
            after = lastCheckTimeStamp?.toEpochMilliseconds(),
        )

        if (drawsByVaultId.values.any { it.isNotEmpty() }) {
            lastCheckTimeStamp = Clock.System.now()
        }

        syncVaults(drawsByVaultId.keys).bind()
        syncPrizes(drawsByVaultId).bind()
    }

    private suspend fun syncPrizes(drawsByVaultId: Map<String, List<GraphDraw>>) = either {
        val vaultById =
            drawsByVaultId.keys.associateWith { vault ->
                vaultRepository.getVaultFromAddress(vault).bind()
            }
        val prizes = drawsByVaultId.flatMap { (vault, draws) ->
            val vault = vaultById.getValue(vault)
            draws.map { draw -> draw.toPrize(draw, vault) }
        }
        prizeRepository.insertPrizes(prizes).bind()
    }

    private suspend fun syncVaults(
        vaultIds: Iterable<String>
    ): Either<RepositoryError, Unit> = either {
        val unknownVaultsAddresses = vaultRepository.findUnknownVaults(vaultIds).onLeft {
            logger.error("Error finding unknown vaults: {}", it)
        }.getOrNull()
        if (unknownVaultsAddresses?.isNotEmpty() == true) {
            logger.info("Found {} unknown vaults", unknownVaultsAddresses.size)
            val newVaults = rpcClient.getVaultInfoFromAdresses(unknownVaultsAddresses)
            if (unknownVaultsAddresses.size != newVaults.size) {
                logger.warn(
                    "Got {} new vaults out of {} unknown",
                    newVaults.size,
                    unknownVaultsAddresses.size,
                )
            }
            vaultRepository.insertVaults(newVaults, ChainNetwork.BASE).bind()
        }
    }
}
