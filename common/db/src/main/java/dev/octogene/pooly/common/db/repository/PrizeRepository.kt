package dev.octogene.pooly.common.db.repository

import arrow.core.Either
import arrow.core.raise.context.ensureNotNull
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
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

interface PrizeRepository {

    suspend fun insertPrizes(prizes: List<Prize>): Either<RepositoryError, Unit>

    suspend fun getAllPrizes(wallets: List<Address>): Either<RepositoryError, List<Prize>>
}

internal class PrizeRepositoryImpl(
    private val database: Database
) : PrizeRepository {

    override suspend fun getAllPrizes(wallets: List<Address>): Either<RepositoryError, List<Prize>> =
        Either.catch {
            suspendTransaction(database, readOnly = true) {
                Prizes
                    .selectAll()
                    .where { Prizes.winnerAddress inList wallets.map { it.value } }
                    .map {
                        with(PrizeEntity.wrapRow(it)) {
                            Prize(
                                payout = amount.toBigInteger(),
                                timestamp = timestamp,
                                winner = Address.unsafeFrom(winnerAddress),
                                transactionHash = transactionHash,
                                vault = Vault(
                                    address = Address.unsafeFrom(vault.id.value),
                                    name = vault.name,
                                    symbol = vault.tokenSymbol,
                                    decimals = vault.tokenDecimals,
                                    network = ChainNetwork.valueOf(network)
                                )
                            )
                        }
                    }
            }
        }.mapLeft { RepositoryError.DatabaseError(it.message ?: "Unknown error") }

    override suspend fun insertPrizes(
        prizes: List<Prize>
    ): Either<RepositoryError, Unit> = suspendTransactionOrRaise(database) {
        prizes.groupBy { it.vault.address }
            .forEach { (vaultAddress, prizesForVault) ->
                val vault = VaultEntity.find { Vaults.id eq vaultAddress.value }.firstOrNull()
                ensureNotNull(vault) { RepositoryError.NotFound("Vault", vaultAddress.value) }
                Prizes.batchInsert(prizesForVault, ignore = true) { prize ->
                    set(vaultId, vault.id.value)
                    set(winnerAddress, prize.winner.value)
                    set(amount, prize.payout.toString())
                    set(transactionHash, prize.transactionHash)
                    set(Prizes.timestamp, prize.timestamp)
                    set(network, prize.vault.network.name)
                }
            }
    }
}
