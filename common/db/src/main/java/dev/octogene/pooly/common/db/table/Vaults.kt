package dev.octogene.pooly.common.db.table

import dev.octogene.pooly.common.db.ADDRESS_LENGTH
import dev.octogene.pooly.common.db.MAX_TOKEN_LENGTH
import dev.octogene.pooly.common.db.MAX_VARCHAR_LENGTH
import dev.octogene.pooly.common.db.NETWORK_NAME_LENGTH
import dev.octogene.pooly.core.Address
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.datetime.timestamp

object Vaults : IdTable<String>("vaults") {
    override val id: Column<EntityID<String>> = varchar("vault_address", ADDRESS_LENGTH).entityId()
    override val primaryKey = PrimaryKey(id)
    val chainNetwork = varchar("chain_network", NETWORK_NAME_LENGTH).index()
    val createdAt = timestamp("created_at")
    val name = varchar("name", MAX_VARCHAR_LENGTH)
    val tokenAddress = varchar("token_address", ADDRESS_LENGTH).index()
    val tokenDecimals = integer("token_decimals")
    val tokenSymbol = varchar("token_symbol", MAX_TOKEN_LENGTH)
}

class VaultEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, VaultEntity>(Vaults)

    val prizes by PrizeEntity referrersOn Prizes.vaultId
    var chainNetwork by Vaults.chainNetwork
    var createdAt by Vaults.createdAt
    var name by Vaults.name
    var tokenAddress by Vaults.tokenAddress.transform(
        unwrap = { it.value },
        wrap = { Address.unsafeFrom(it) },
    )
    var tokenDecimals by Vaults.tokenDecimals
    var tokenSymbol by Vaults.tokenSymbol
}
