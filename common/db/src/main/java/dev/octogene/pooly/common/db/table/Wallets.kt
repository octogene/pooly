package dev.octogene.pooly.common.db.table

import dev.octogene.pooly.common.db.ADDRESS_LENGTH
import dev.octogene.pooly.common.db.table.Wallets.userId
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.datetime.timestamp

object Wallets : CompositeIdTable("wallet_addresses") {
    val address = varchar("address", ADDRESS_LENGTH).entityId()
    val createdAt = timestamp("created_at")
    val userId = long("user_id").references(Users.id).entityId()

    override val primaryKey = PrimaryKey(userId, address)
}

class WalletEntity(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : EntityClass<CompositeID, WalletEntity>(Wallets)

    var address by Wallets.address
    var createdAt by Wallets.createdAt
    var userId by Wallets.userId
}
