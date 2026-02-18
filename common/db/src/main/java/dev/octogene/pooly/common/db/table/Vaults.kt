package dev.octogene.pooly.common.db.table

import dev.octogene.pooly.common.db.ADDRESS_LENGTH
import dev.octogene.pooly.common.db.MAX_TOKEN_LENGTH
import dev.octogene.pooly.common.db.MAX_VARCHAR_LENGTH
import dev.octogene.pooly.common.db.NETWORK_NAME_LENGTH
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.ChainNetwork
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.dao.ImmutableCachedEntityClass
import org.jetbrains.exposed.v1.dao.ImmutableEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp

object Vaults : IdTable<String>("vaults") {
    override val id: Column<EntityID<String>> = varchar("vault_address", ADDRESS_LENGTH).entityId()
    override val primaryKey = PrimaryKey(id)
    val chainNetwork = enumeration<ChainNetwork>("chain_network").index()
    val createdAt = timestamp("created_at")
    val name = varchar("name", MAX_VARCHAR_LENGTH)
    val tokenAddress = varchar("token_address", ADDRESS_LENGTH).index()
    val tokenDecimals = integer("token_decimals")
    val tokenSymbol = varchar("token_symbol", MAX_TOKEN_LENGTH)
}

class VaultEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : ImmutableCachedEntityClass<String, VaultEntity>(table = Vaults)

    val prizes by PrizeEntity referrersOn Prizes.vaultId
    val chainNetwork by Vaults.chainNetwork
    val createdAt by Vaults.createdAt
    val name by Vaults.name
    val tokenAddress by Vaults.tokenAddress.transform(
        unwrap = { it.value },
        wrap = { Address.unsafeFrom(it) },
    )
    val tokenDecimals by Vaults.tokenDecimals
    val tokenSymbol by Vaults.tokenSymbol
}
