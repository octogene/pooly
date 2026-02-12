package dev.octogene.pooly.server.test

import arrow.core.Either
import dev.octogene.pooly.common.db.repository.RepositoryError
import dev.octogene.pooly.common.db.repository.UserRepository
import dev.octogene.pooly.core.PasswordHash
import dev.octogene.pooly.core.User
import dev.octogene.pooly.core.UserRole
import dev.octogene.pooly.core.UserWithWallets

class FakeUserRepository(private val users: MutableMap<String, User> = mutableMapOf()) : UserRepository {

    override suspend fun createUser(
        username: String,
        email: String,
        passwordHash: PasswordHash,
        role: UserRole,
    ): Either<RepositoryError, Unit> {
        users[username] = User(username, email, passwordHash, role)
        return Either.Right(Unit)
    }

    override suspend fun removeUser(username: String): Either<RepositoryError, Unit> {
        users.remove(username)
        return Either.Right(Unit)
    }

    override suspend fun findUserByUsername(username: String): Either<RepositoryError, dev.octogene.pooly.core.User> =
        if (users.containsKey(username)) {
            Either.Right(users.getValue(username))
        } else {
            Either.Left(RepositoryError.NotFound("User", username))
        }

    override suspend fun getUserWithWallets(username: String): Either<RepositoryError, UserWithWallets> {
        TODO("Not yet implemented")
    }
}
