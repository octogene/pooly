package dev.octogene.pooly.common.db.repository

import arrow.core.Either
import arrow.core.raise.context.ensureNotNull
import dev.octogene.pooly.common.db.suspendTransactionOrRaise
import dev.octogene.pooly.common.db.table.UserEntity
import dev.octogene.pooly.common.db.table.Users
import dev.octogene.pooly.common.db.table.WalletEntity
import dev.octogene.pooly.common.db.table.Wallets
import dev.octogene.pooly.common.db.table.Wallets.userId
import dev.octogene.pooly.core.Address
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import kotlin.time.Clock

interface WalletRepository {
    suspend fun getAllWalletAddresses(): Either<RepositoryError, List<Address>>
    suspend fun addWallets(username: String, addresses: List<Address>): Either<RepositoryError, Unit>

    suspend fun removeWallets(username: String, addresses: List<Address>): Either<RepositoryError, Int>

    suspend fun getWalletsForUser(username: String): Either<RepositoryError, List<Address>>
}

internal class WalletRepositoryImpl(private val database: Database) : WalletRepository {

    override suspend fun getAllWalletAddresses(): Either<RepositoryError, List<Address>> = Either.catch {
        suspendTransaction(database) {
            WalletEntity.wrapRows(Wallets.select(Wallets.address).withDistinct(true))
                .map { Address.unsafeFrom(it.address.value) }
                .toList()
        }
    }.mapLeft { RepositoryError.DatabaseError(it.message ?: "Unknown error") }

    override suspend fun addWallets(username: String, addresses: List<Address>): Either<RepositoryError, Unit> =
        suspendTransactionOrRaise(database) {
            val user = UserEntity.find { Users.username eq username }.firstOrNull()
            ensureNotNull(user) { RepositoryError.NotFound("User", username) }
            val createdAt = Clock.System.now()
            Wallets.batchInsert(addresses, ignore = true) { address ->
                set(Wallets.userId, user.id.value)
                set(Wallets.address, address.value)
                set(Wallets.createdAt, createdAt)
            }
        }

    override suspend fun removeWallets(username: String, addresses: List<Address>): Either<RepositoryError, Int> =
        suspendTransactionOrRaise(database) {
            val user = UserEntity.find { Users.username eq username }.firstOrNull()
            ensureNotNull(user) { RepositoryError.NotFound("User", username) }
            Wallets.deleteWhere {
                (Wallets.userId eq user.id.value) and (Wallets.address inList addresses.map { it.value })
            }
        }

    override suspend fun getWalletsForUser(username: String): Either<RepositoryError, List<Address>> =
        suspendTransactionOrRaise(database) {
            val user = UserEntity.find { Users.username eq username }.firstOrNull()
            ensureNotNull(user) { RepositoryError.NotFound("User", username) }
            WalletEntity
                .find { Wallets.userId eq userId }
                .map { Address.unsafeFrom(it.address.value) }
                .toList()
        }
}
