package dev.octogene.pooly.common.db.table

import dev.octogene.pooly.common.db.ADDRESS_LENGTH
import dev.octogene.pooly.common.db.MAX_TOKEN_LENGTH
import dev.octogene.pooly.common.db.NETWORK_NAME_LENGTH
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object Vaults : IdTable<String>("vaults") {
    override val id: Column<EntityID<String>> = varchar("vault_address", ADDRESS_LENGTH).entityId()
    val chainNetwork = varchar("chain_network", NETWORK_NAME_LENGTH).index()
    val tokenAddress = varchar("token_address", ADDRESS_LENGTH).index()
    val tokenSymbol = varchar("token_symbol", MAX_TOKEN_LENGTH)
    val tokenDecimals = integer("token_decimals")
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}

class VaultEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, VaultEntity>(Vaults)

    var chainNetwork by Vaults.chainNetwork
    var tokenAddress by Vaults.tokenAddress
    var tokenSymbol by Vaults.tokenSymbol
    var tokenDecimals by Vaults.tokenDecimals
    var createdAt by Vaults.createdAt

    val prizes by PrizeEntity referrersOn Prizes.vaultId
}
