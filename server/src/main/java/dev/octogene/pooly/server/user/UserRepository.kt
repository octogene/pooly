package dev.octogene.pooly.server.user

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import dev.octogene.pooly.common.db.table.Prizes
import dev.octogene.pooly.common.db.table.UserEntity
import dev.octogene.pooly.common.db.table.Users
import dev.octogene.pooly.common.db.table.Vaults
import dev.octogene.pooly.common.db.table.Wallets
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.server.model.DatabaseError
import dev.octogene.pooly.server.model.DatabaseError.OperationError.NotFound
import dev.octogene.pooly.server.security.PasswordHasher
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock

interface UserRepository {
    fun createUser(name: String, email: String, password: String): Either<DatabaseError, Unit>
    fun addWallets(username: String, addresses: List<Address>): Either<DatabaseError, Unit>
    fun removeWallets(username: String, addresses: List<Address>): Either<DatabaseError, Unit>
    fun getWallets(username: String): Either<DatabaseError, List<Address>>
}

class UserRepositoryImpl(
    private val database: Database,
    private val passwordHasher: PasswordHasher,
    private val logger: Logger = LoggerFactory.getLogger(UserRepositoryImpl::class.java)
) : UserRepository {

    init {
        transaction(database) {
            SchemaUtils.create(Users, Wallets, Vaults, Prizes)
        }
    }

    override fun createUser(name: String, email: String, password: String): Either<DatabaseError, Unit> = either {
        logger.debug("Creating user $name")
        transaction(database) {
            Users.insert {
                it[Users.username] = name
                it[Users.email] = email
                it[Users.passwordHash] = passwordHasher.hashPassword(password)
                it[Users.createdAt] = Clock.System.now()
            }
        }
    }

    override fun addWallets(
        username: String,
        addresses: List<Address>
    ): Either<DatabaseError, Unit> = either {
        transaction(database) {
            val userId = Users
                .select(Users.id)
                .where(Users.username eq username)
                .singleOrNull()?.get(Users.id)

            ensureNotNull(userId) { NotFound("User", username) }

            val createdAt = Clock.System.now()

            Wallets.batchInsert(addresses, ignore = true) { address ->
                set(Wallets.userId, userId.value)
                set(Wallets.id, address.value)
                set(Wallets.createdAt, createdAt)
            }
        }
    }

    override fun removeWallets(username: String, addresses: List<Address>): Either<DatabaseError, Unit> = either {
        transaction(database) {
            val userId = Users
                .select(Users.id)
                .where(Users.username eq username)
                .singleOrNull()?.get(Users.id)

            ensureNotNull(userId) { NotFound("User", username) }

            Wallets.deleteWhere {
                (Wallets.userId eq userId.value) and (Wallets.id inList addresses.map { it.value })
            }
        }
    }

    override fun getWallets(username: String): Either<DatabaseError, List<Address>> = either {
        val wallets = transaction(database) {
            val user = UserEntity.find { Users.username eq username }.singleOrNull()
            ensureNotNull(user) { NotFound("User", username) }
            user.walletAddresses.map { Address.unsafeFrom(it.id.value) }
        }
        wallets
    }
}
