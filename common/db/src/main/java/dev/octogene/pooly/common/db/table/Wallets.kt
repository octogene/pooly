package dev.octogene.pooly.common.db.table

import dev.octogene.pooly.common.db.ADDRESS_LENGTH
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.datetime.timestamp

object Wallets : IdTable<String>("wallet_addresses") {
    override val id: Column<EntityID<String>> = varchar("address", ADDRESS_LENGTH).entityId()
    override val primaryKey = PrimaryKey(id)
    val createdAt = timestamp("created_at")
    val userId = long("user_id").references(Users.id)
}

class WalletEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, WalletEntity>(Wallets)

    var createdAt by Wallets.createdAt
    var userId by Wallets.userId
}
