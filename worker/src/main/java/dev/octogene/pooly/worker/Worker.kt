package dev.octogene.pooly.worker

import dev.octogene.pooly.common.db.table.Prizes
import dev.octogene.pooly.common.db.table.Users
import dev.octogene.pooly.common.db.table.VaultEntity
import dev.octogene.pooly.common.db.table.Vaults
import dev.octogene.pooly.common.db.table.WalletAddress
import dev.octogene.pooly.common.db.table.WalletAddresses
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.core.Vault
import dev.octogene.pooly.ptgraph.api.PoolTogetherGraphQLClient
import dev.octogene.pooly.ptgraph.api.model.GraphDraw
import dev.octogene.pooly.rpc.PoolTogetherRPCClient
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.exists
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class Worker(
    private val rpcClient: PoolTogetherRPCClient,
    private val graphClient: PoolTogetherGraphQLClient,
    private val database: Database,
    private val logger: Logger = LoggerFactory.getLogger(Worker::class.java)
) {
    private var lastCheckTimeStamp: Instant? = null
    suspend fun run() = coroutineScope {
        transaction(database) {
            checkTablesExists()
        }
        logger.info("Starting worker")

        while (isActive) {
            val addresses = getAllWalletAddresses().map { it.address }
            if (addresses.isNotEmpty()) {
                val (draws, newVaults) = fetchDrawsAndVaults(addresses)
                syncVaultsAndPrizes(newVaults, draws)
            }
            lastCheckTimeStamp = Clock.System.now()
            delay(5.minutes)
        }
    }

    private fun syncVaultsAndPrizes(
        newVaults: List<Vault>,
        draws: List<GraphDraw>
    ) {
        transaction(database) {
            newVaults.forEach { vault ->
                Vaults.insert {
                    it[id] = vault.address.value
                    it[chainNetwork] = ChainNetwork.BASE.name
                    it[tokenAddress] = vault.address.value
                    it[tokenSymbol] = vault.symbol
                    it[tokenDecimals] = vault.decimals.toInt()
                    it[createdAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                }
            }

            draws.groupBy { it.vault }.forEach { (vaultAddress, draws) ->
                VaultEntity.find {
                    Vaults.id eq vaultAddress
                }.firstOrNull()?.let { vault ->
                    // TODO: For whatever reason the graph returns duplicate draws.
                    //  We filter them out here, but it should be fixed upstream.
                    draws.distinctBy { it.transactionHash }.forEach { draw ->
                        Prizes.insert {
                            it[vaultId] = vault.id.value
                            it[winnerAddress] = draw.winner
                            it[amount] = draw.payout.toString()
                            it[transactionHash] = draw.transactionHash
                            it[timestamp] = draw.timestamp
                            it[network] = ChainNetwork.BASE.name
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchDrawsAndVaults(addresses: List<String>): Pair<List<GraphDraw>, List<Vault>> {
        logger.info("Getting draws for {} addresses", addresses.size)
        val draws = graphClient.getAllDraws(
            addresses = addresses,
            chainNetwork = ChainNetwork.BASE,
            after = lastCheckTimeStamp?.toEpochMilliseconds()
        )
        logger.info("Found {} draws", draws.size)
        val unknownVaultsAddresses = findUnknownVaults(draws.map { it.vault }.distinct())
        logger.info("found {} unknown vaults", unknownVaultsAddresses.size)
        val newVaults = rpcClient.getVaultInfoFromAdresses(unknownVaultsAddresses)
        logger.info("found {} new vaults", newVaults.size)
        return Pair(draws, newVaults)
    }

    private fun checkTablesExists() {
        if (!Prizes.exists()) {
            SchemaUtils.create(Users, WalletAddresses, Vaults, Prizes)
        }
    }

    fun getAllWalletAddresses(): List<WalletAddress> {
        return try {
            transaction(database) {
                WalletAddress.wrapRows(WalletAddresses.selectAll()).toList()
            }
        } catch (e: Exception) {
            logger.error("Error fetching all wallet addresses : {}", e.message)
            emptyList()
        }
    }

    fun findUnknownVaults(expectedVaultsAddress: List<String>): List<String> {
        return try {
            transaction(database) {
                val knownVaultIdentifiers = Vaults.select(Vaults.id)
                    .where { Vaults.id inList expectedVaultsAddress }
                    .map { it[Vaults.id].value }
                    .toSet()

                expectedVaultsAddress.filterNot { expected ->
                    expected in knownVaultIdentifiers
                }
            }
        } catch (e: Exception) {
            logger.error("Error finding unknown vaults: {}", e.message)
            emptyList()
        }
    }
}
