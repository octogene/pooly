package dev.octogene.pooly.common.db.repository

import arrow.core.Either
import dev.octogene.pooly.common.db.suspendTransactionOrRaise
import dev.octogene.pooly.common.db.table.VaultEntity
import dev.octogene.pooly.common.db.table.Vaults
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.core.Vault
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.select
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock

interface VaultRepository {
    suspend fun getVaultFromAddress(address: String): Either<RepositoryError, Vault>
    suspend fun findUnknownVaults(expectedVaultAddresses: List<String>): Either<RepositoryError, List<String>>
    suspend fun insertVaults(
        vaults: List<Vault>,
        network: ChainNetwork
    ): Either<RepositoryError, Unit>
}

internal class VaultRepositoryImpl(
    private val database: Database,
    private val logger: Logger = LoggerFactory.getLogger(VaultRepository::class.java)
) : VaultRepository {
    override suspend fun getVaultFromAddress(address: String): Either<RepositoryError, Vault> =
        suspendTransactionOrRaise(database) {
            val vault = VaultEntity.find { Vaults.id eq address }.single()
            Vault(
                Address.unsafeFrom(vault.id.value),
                vault.name,
                vault.tokenSymbol,
                vault.tokenDecimals,
                ChainNetwork.valueOf(vault.chainNetwork)
            )
        }

    override suspend fun findUnknownVaults(expectedVaultAddresses: List<String>): Either<RepositoryError, List<String>> =
        suspendTransactionOrRaise(database, readOnly = true) {
            val knownVaultIds = Vaults
                .select(Vaults.id)
                .where { Vaults.id inList expectedVaultAddresses }
                .map { it[Vaults.id].value }
                .toSet()

            expectedVaultAddresses.filterNot { it in knownVaultIds }
        }

    override suspend fun insertVaults(
        vaults: List<Vault>,
        network: ChainNetwork
    ): Either<RepositoryError, Unit> =
        suspendTransactionOrRaise(database) {
            if (vaults.isEmpty()) {
                logger.debug("No vaults to insert")
                return@suspendTransactionOrRaise
            }

            Vaults.batchInsert(vaults, ignore = true) { vault ->
                set(Vaults.id, vault.address.value)
                set(Vaults.name, vault.name)
                set(Vaults.chainNetwork, network.name)
                set(Vaults.tokenAddress, vault.address.value)
                set(Vaults.tokenSymbol, vault.symbol)
                set(Vaults.tokenDecimals, vault.decimals)
                set(Vaults.createdAt, Clock.System.now())
            }
        }
}
