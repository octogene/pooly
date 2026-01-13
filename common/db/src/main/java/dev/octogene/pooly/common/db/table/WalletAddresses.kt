package dev.octogene.pooly.common.db.table

import dev.octogene.pooly.common.db.ADDRESS_LENGTH
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.UUID

object WalletAddresses : UUIDTable("wallet_addresses") {
    val userId = long("user_id").references(Users.id)
    val address = varchar("address", ADDRESS_LENGTH).uniqueIndex()
    val createdAt = datetime("created_at")
}

class WalletAddress(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<WalletAddress>(WalletAddresses)

    var userId by WalletAddresses.userId
    var address by WalletAddresses.address
    var createdAt by WalletAddresses.createdAt
}
