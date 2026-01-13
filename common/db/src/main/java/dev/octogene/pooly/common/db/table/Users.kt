package dev.octogene.pooly.common.db.table

import dev.octogene.pooly.common.db.MAX_USERNAME_LENGTH
import dev.octogene.pooly.common.db.MAX_VARCHAR_LENGTH
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object Users : LongIdTable("users") {
    val username = varchar("username", MAX_USERNAME_LENGTH).uniqueIndex()
    val email = varchar("email", MAX_VARCHAR_LENGTH).nullable()
    val createdAt = datetime("created_at")
}

class User(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<User>(Users)

    var username by Users.username
    var email by Users.email
    var createdAt by Users.createdAt

    // A user has a list of wallet addresses
    val walletAddresses by WalletAddress referrersOn WalletAddresses.userId
}
