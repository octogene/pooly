package dev.octogene.pooly.common.db.repository

import arrow.core.Either
import arrow.core.raise.context.ensure
import dev.octogene.pooly.common.db.suspendTransactionOrRaise
import dev.octogene.pooly.common.db.table.PrizeEntity
import dev.octogene.pooly.common.db.table.Prizes
import dev.octogene.pooly.common.db.table.Prizes.amount
import dev.octogene.pooly.common.db.table.Prizes.network
import dev.octogene.pooly.common.db.table.Prizes.transactionHash
import dev.octogene.pooly.common.db.table.Prizes.vaultId
import dev.octogene.pooly.common.db.table.Prizes.winnerAddress
import dev.octogene.pooly.common.db.table.VaultEntity
import dev.octogene.pooly.common.db.table.Vaults
import dev.octogene.pooly.common.db.table.toPrize
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.Amount
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface PrizeRepository {

    suspend fun insertPrizes(prizes: List<Prize>): Either<RepositoryError, Unit>

    suspend fun getAllPrizes(wallets: List<Address>): Either<RepositoryError, List<Prize>>

    suspend fun getAllPrizesPaged(
        wallets: List<Address>,
        pageRequest: PageRequest,
    ): Either<RepositoryError, Page<Prize>>
}

internal class PrizeRepositoryImpl(
    private val database: Database,
    private val logger: Logger = LoggerFactory.getLogger(PrizeRepository::class.java),
) : PrizeRepository {

    // TODO: Test Exposed caching for Vault by using the DAO
    override suspend fun getAllPrizes(wallets: List<Address>): Either<RepositoryError, List<Prize>> =
        suspendTransactionOrRaise(database, readOnly = true) {
            val walletRawAddresses = wallets.map { it.value }
            val allPrizesWithVaultsQuery = Prizes.join(
                Vaults,
                onColumn = vaultId,
                otherColumn = Vaults.id,
                joinType = JoinType.INNER,
            ).selectAll()
                .where { winnerAddress inList walletRawAddresses }
                .orderBy(Prizes.timestamp to SortOrder.DESC)

            val vaults = mutableMapOf<String, Vault>()
            allPrizesWithVaultsQuery.map { resultRow ->
                val vaultId = resultRow[Vaults.id].value
                val vault = vaults.getOrPut(vaultId) {
                    Vault(
                        address = Address.unsafeFrom(resultRow[Vaults.id].value),
                        name = resultRow[Vaults.name],
                        symbol = resultRow[Vaults.tokenSymbol],
                        decimals = resultRow[Vaults.tokenDecimals],
                        network = resultRow[Vaults.chainNetwork],
                    )
                }
                Prize(
                    payout = Amount.from(resultRow[amount]),
                    timestamp = resultRow[Prizes.timestamp],
                    winner = Address.unsafeFrom(resultRow[winnerAddress]),
                    vault = vault,
                    transactionHash = resultRow[transactionHash],
                )
            }
        }

    override suspend fun getAllPrizesPaged(
        wallets: List<Address>,
        pageRequest: PageRequest,
    ): Either<RepositoryError, Page<Prize>> = suspendTransactionOrRaise(database, readOnly = true) {
        val walletRawAddresses = wallets.map { it.value }
        val totalCount = Prizes
            .selectAll()
            .where { winnerAddress inList walletRawAddresses }
            .count()

        val items = Prizes
            .selectAll()
            .where { winnerAddress inList walletRawAddresses }
            .orderBy(Prizes.timestamp to SortOrder.DESC)
            .offset(pageRequest.offset)
            .limit(pageRequest.pageSize)
            .map { PrizeEntity.wrapRow(it).toPrize() }

        Page(
            items = items,
            totalCount = totalCount,
            page = pageRequest.page,
            pageSize = pageRequest.pageSize,
        )
    }

    override suspend fun insertPrizes(prizes: List<Prize>): Either<RepositoryError, Unit> =
        suspendTransactionOrRaise(database) {
            if (prizes.isEmpty()) {
                logger.debug("No prizes to insert")
                return@suspendTransactionOrRaise
            }

            val uniqueVaultAddresses = prizes.map { it.vault.address.value }.distinct()
            logger.debug("Inserting {} prizes for {} vaults", prizes.size, uniqueVaultAddresses.size)

            val vaults = VaultEntity.find { Vaults.id inList uniqueVaultAddresses }
                .associateBy { it.id.value }
            val missingVaults = uniqueVaultAddresses.filter { it !in vaults.keys }

            ensure(missingVaults.isEmpty()) {
                logger.error("Found {} missing vaults", missingVaults.size)
                logger.debug("Missing vaults: {}", missingVaults)
                RepositoryError.NotFound("Vault", missingVaults.joinToString(", "))
            }

            Prizes.batchInsert(prizes, ignore = true, shouldReturnGeneratedValues = false) { prize ->
                val vault = vaults.getValue(prize.vault.address.value)
                set(vaultId, vault.id.value)
                set(winnerAddress, prize.winner.value)
                set(amount, prize.payout.toString())
                set(transactionHash, prize.transactionHash)
                set(Prizes.timestamp, prize.timestamp)
                set(network, prize.vault.network)
            }
        }
}
