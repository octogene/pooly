package dev.octogene.pooly.server.test

import arrow.core.Either
import dev.octogene.pooly.common.db.repository.RepositoryError
import dev.octogene.pooly.common.db.repository.WalletRepository
import dev.octogene.pooly.core.Address

class FakeWalletRepository(private val wallets: MutableMap<String, MutableList<Address>> = mutableMapOf()) :
    WalletRepository {

    override suspend fun getAllWalletAddresses(): Either<RepositoryError, List<Address>> = Either.Right(
        wallets.flatMap {
            it.value
        },
    )

    override suspend fun addWallets(username: String, addresses: List<Address>): Either<RepositoryError, Unit> {
        wallets.getOrPut(username) { mutableListOf() }.addAll(addresses)
        return Either.Right(Unit)
    }

    override suspend fun removeWallets(username: String, addresses: List<Address>): Either<RepositoryError, Int> {
        wallets[username]?.removeAll(addresses)
        return Either.Right(addresses.size)
    }

    override suspend fun getWalletsForUser(username: String): Either<RepositoryError, List<Address>> =
        Either.Right(wallets[username].orEmpty())
}
