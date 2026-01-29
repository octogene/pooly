package dev.octogene.pooly.server.test

import arrow.core.Either
import dev.octogene.pooly.common.db.repository.RepositoryError
import dev.octogene.pooly.common.db.repository.UserRepository
import dev.octogene.pooly.core.User
import dev.octogene.pooly.core.UserWithWallets
import dev.octogene.pooly.server.security.PasswordHasher

class FakeUserRepository(
    private val users: MutableMap<String, User> = mutableMapOf(),
    private val passwordHasher: PasswordHasher
) : UserRepository {

    override suspend fun createUser(
        name: String,
        email: String,
        password: String
    ): Either<RepositoryError, Unit> {
        users[name] = User(name, email, passwordHasher.hash(password))
        return Either.Right(Unit)
    }

    override suspend fun findUserByUsername(username: String): Either<RepositoryError, dev.octogene.pooly.core.User> {
        return if (users.containsKey(username)) {
            Either.Right(users.getValue(username))
        } else {
            Either.Left(RepositoryError.NotFound("User", username))
        }
    }

    override suspend fun getUserWithWallets(username: String): Either<RepositoryError, UserWithWallets> {
        TODO("Not yet implemented")
    }
}