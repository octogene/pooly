package dev.octogene.pooly.common.db.repository

import arrow.core.Either
import arrow.core.raise.context.ensureNotNull
import dev.octogene.pooly.common.db.suspendTransactionOrRaise
import dev.octogene.pooly.common.db.table.UserEntity
import dev.octogene.pooly.common.db.table.Users
import dev.octogene.pooly.common.db.table.Users.role
import dev.octogene.pooly.common.db.table.Wallets
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.PasswordHash
import dev.octogene.pooly.core.User
import dev.octogene.pooly.core.UserRole
import dev.octogene.pooly.core.UserWithWallets
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock

interface UserRepository {
    suspend fun createUser(
        username: String,
        email: String,
        passwordHash: PasswordHash,
        role: UserRole = UserRole.USER,
    ): Either<RepositoryError, Unit>

    suspend fun removeUser(username: String): Either<RepositoryError, Unit>

    suspend fun findUserByUsername(username: String): Either<RepositoryError, User>

    suspend fun getUserWithWallets(username: String): Either<RepositoryError, UserWithWallets>
}

internal class UserRepositoryImpl(
    private val database: Database,
    private val logger: Logger = LoggerFactory.getLogger(UserRepositoryImpl::class.java),
) : UserRepository {

    override suspend fun createUser(
        username: String,
        email: String,
        passwordHash: PasswordHash,
        role: UserRole,
    ): Either<RepositoryError, Unit> = suspendTransactionOrRaise(database) {
        logger.debug("Creating user : {}", username)
        Users.insert {
            it[Users.username] = username
            it[Users.email] = email
            it[Users.passwordHash] = passwordHash.content
            it[Users.createdAt] = Clock.System.now()
            it[Users.role] = role
        }
    }

    override suspend fun removeUser(username: String): Either<RepositoryError, Unit> =
        suspendTransactionOrRaise(database) {
            logger.debug("Removing user : {}", username)
            val userEntity = UserEntity.find { Users.username eq username }.singleOrNull()
            ensureNotNull(userEntity) { RepositoryError.NotFound("User", username) }
            Wallets.deleteWhere { Wallets.userId eq userEntity.id.value }
            Users.deleteWhere { Users.username eq username }
        }

    override suspend fun findUserByUsername(username: String): Either<RepositoryError, User> =
        suspendTransactionOrRaise(database) {
            val statement = Users
                .select(
                    Users.id,
                    Users.username,
                    Users.email,
                    Users.passwordHash,
                    role,
                )
                .where { Users.username eq username }
            val resultRow = statement.singleOrNull()
            ensureNotNull(resultRow) { RepositoryError.NotFound("User", username) }

            User(
                username = resultRow[Users.username],
                email = resultRow[Users.email],
                passwordHash = PasswordHash(resultRow[Users.passwordHash]),
                role = resultRow[role],
            )
        }

    override suspend fun getUserWithWallets(username: String): Either<RepositoryError, UserWithWallets> =
        suspendTransactionOrRaise(database, readOnly = true) {
            val userEntity = UserEntity.find { Users.username eq username }.singleOrNull()
            ensureNotNull(userEntity) { RepositoryError.NotFound("User", username) }

            val user = User(
                username = userEntity.username,
                email = userEntity.email,
                passwordHash = PasswordHash(userEntity.passwordHash),
                role = userEntity.role,
            )
            val wallets = userEntity.walletAddresses.map { Address.unsafeFrom(it.address.value) }

            UserWithWallets(user, wallets)
        }
}
