package dev.octogene.pooly.common.db.table

import dev.octogene.pooly.common.db.MAX_USERNAME_LENGTH
import dev.octogene.pooly.common.db.MAX_VARCHAR_LENGTH
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp

object Users : LongIdTable("users") {
    val createdAt = timestamp("created_at")
    val email = varchar("email", MAX_VARCHAR_LENGTH).nullable()
    val passwordHash = varchar("password_hash", MAX_VARCHAR_LENGTH)
    val username = varchar("username", MAX_USERNAME_LENGTH).uniqueIndex()
}

class UserEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserEntity>(Users)

    val walletAddresses by WalletEntity referrersOn Wallets.userId
    var createdAt by Users.createdAt
    var email by Users.email
    var passwordHash by Users.passwordHash
    var username by Users.username
}
