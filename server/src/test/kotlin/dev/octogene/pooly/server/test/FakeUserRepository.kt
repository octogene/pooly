package dev.octogene.pooly.server.test

import arrow.core.Either
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.server.model.DatabaseError
import dev.octogene.pooly.server.user.User
import dev.octogene.pooly.server.user.UserRepository

class FakeUserRepository(
    private val users: MutableMap<String, User> = mutableMapOf<String, User>()
): UserRepository {
    private val wallets = mutableMapOf<User, MutableList<Address>>()

    override fun createUser(
        name: String,
        email: String,
        password: String
    ): Either<DatabaseError, Unit> {
        users[name] = User(name, email, password)
        return Either.Right(Unit)
    }

    override fun addWallets(
        username: String,
        addresses: List<Address>
    ): Either<DatabaseError, Unit> {
        users[username]?.let { user ->
            wallets.getOrPut(user) { mutableListOf() }.addAll(addresses)
        } ?: return Either.Left(DatabaseError.OperationError.NotFound("User", username))
        return Either.Right(Unit)
    }

    override fun removeWallets(
        username: String,
        addresses: List<Address>
    ): Either<DatabaseError, Unit> {
        users[username]?.let { user ->
            wallets[user]?.removeAll(addresses)
        } ?: return Either.Left(DatabaseError.OperationError.NotFound("User", username))
        return Either.Right(Unit)
    }

    override fun getWallets(username: String): Either<DatabaseError, List<Address>> {
        return users[username]?.let { user ->
            Either.Right(wallets[user] ?: emptyList())
        } ?: Either.Left(DatabaseError.OperationError.NotFound("User", username))
    }
}