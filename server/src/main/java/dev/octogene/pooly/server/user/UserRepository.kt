package dev.octogene.pooly.server.user

import dev.octogene.pooly.common.db.table.Prizes
import dev.octogene.pooly.common.db.table.Users
import dev.octogene.pooly.common.db.table.Vaults
import dev.octogene.pooly.common.db.table.Wallets
import dev.octogene.pooly.server.security.PasswordHasher
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Clock
import dev.octogene.pooly.common.db.table.UserEntity as UserEntity

interface UserRepository {
    fun createUser(name: String, email: String, password: String)
    fun addWallets(username: String, addresses: List<String>)
    fun removeWallets(username: String, addresses: List<String>)

    fun getWallets(username: String): List<String>
}

class UserRepositoryImpl(
    private val database: Database,
    private val passwordHasher: PasswordHasher
) : UserRepository {

    init {
        transaction(database) {
            SchemaUtils.create(Users, Wallets, Vaults, Prizes)
        }
    }

    override fun createUser(name: String, email: String, password: String) {
        transaction(database) {
            Users.insert {
                it[Users.username] = name
                it[Users.email] = email
                it[Users.passwordHash] = passwordHasher.hashPassword(password)
                it[Users.createdAt] = Clock.System.now()
            }
        }
    }

    override fun addWallets(username: String, addresses: List<String>) {
        transaction(database) {
            val userId = Users
                .select(Users.id)
                .where(Users.username eq username)
                .single()[Users.id]

            val createdAt = Clock.System.now()

            addresses.forEach { address ->
                Wallets.insert {
                    it[Wallets.userId] = userId.value
                    it[Wallets.id] = address
                    it[Wallets.createdAt] = createdAt
                }
            }
        }
    }

    override fun removeWallets(username: String, addresses: List<String>) {
        transaction(database) {
            val userId = Users
                .select(Users.id)
                .where(Users.username eq username)
                .single()[Users.id]

            Wallets.deleteWhere {
                (Wallets.userId eq userId.value) and (Wallets.id inList addresses)
            }
        }
    }

    override fun getWallets(username: String): List<String> {
        val wallets = transaction(database) {
            val user = UserEntity.find { Users.username eq username }.single()
            user.walletAddresses.map { it.id.value }
        }
        return wallets
    }
}
