package dev.octogene.pooly.common.db.repository

import arrow.core.Either
import arrow.core.raise.context.ensureNotNull
import dev.octogene.pooly.common.db.suspendTransactionOrRaise
import dev.octogene.pooly.common.db.table.UserEntity
import dev.octogene.pooly.common.db.table.Users
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.User
import dev.octogene.pooly.core.UserWithWallets
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.time.Clock

interface UserRepository {
    suspend fun createUser(
        username: String,
        email: String,
        password: String,
    ): Either<RepositoryError, Unit>

    suspend fun findUserByUsername(username: String): Either<RepositoryError, User>

    suspend fun getUserWithWallets(username: String): Either<RepositoryError, UserWithWallets>
}

internal class UserRepositoryImpl(
    private val database: Database,
    private val passwordHasher: (String) -> String
) : UserRepository {

    override suspend fun createUser(
        username: String,
        email: String,
        password: String
    ): Either<RepositoryError, Unit> = suspendTransactionOrRaise(database) {
        Users.insert {
            it[Users.username] = username
            it[Users.email] = email
            it[Users.passwordHash] = passwordHasher(password)
            it[Users.createdAt] = Clock.System.now()
        }
    }

    override suspend fun findUserByUsername(
        username: String
    ): Either<RepositoryError, User> = suspendTransactionOrRaise(database) {
        val resultRow = Users
            .select(
                Users.id,
                Users.username,
                Users.email,
                Users.passwordHash
            )
            .where { Users.username eq username }
            .singleOrNull()

        ensureNotNull(resultRow) { RepositoryError.NotFound("User", username) }

        User(
            username = resultRow[Users.username],
            email = resultRow[Users.email],
            passwordHash = resultRow[Users.passwordHash]
        )
    }

    override suspend fun getUserWithWallets(
        username: String
    ): Either<RepositoryError, UserWithWallets> =
        suspendTransactionOrRaise(database, readOnly = true) {
            val userEntity = UserEntity.find { Users.username eq username }.singleOrNull()
            ensureNotNull(userEntity) { RepositoryError.NotFound("User", username) }

            val user = User(
                username = userEntity.username,
                email = userEntity.email,
                passwordHash = userEntity.passwordHash
            )
            val wallets = userEntity.walletAddresses.map { Address.unsafeFrom(it.id.value) }

            UserWithWallets(user, wallets)
        }
}
