package dev.octogene.pooly.common.db.table

import dev.octogene.pooly.common.db.ADDRESS_LENGTH
import dev.octogene.pooly.common.db.MAX_AMOUNT_LENGTH
import dev.octogene.pooly.common.db.NETWORK_NAME_LENGTH
import dev.octogene.pooly.common.db.TX_HASH_LENGTH
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp

object Prizes : LongIdTable("prizes") {
    val amount = varchar("amount", MAX_AMOUNT_LENGTH)
    val network = varchar("network", NETWORK_NAME_LENGTH).index()
    val timestamp = timestamp("timestamp")
    val transactionHash = varchar("transaction_hash", TX_HASH_LENGTH).index()
    val vaultId = varchar("vault_id", ADDRESS_LENGTH).references(Vaults.id) // Foreign key to Vaults
    val winnerAddress = varchar("winner_address", ADDRESS_LENGTH)

    init {
        uniqueIndex(vaultId, transactionHash, winnerAddress)
    }
}

class PrizeEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<PrizeEntity>(Prizes)

    var amount by Prizes.amount
    var network by Prizes.network
    var timestamp by Prizes.timestamp
    var transactionHash by Prizes.transactionHash
    var vaultId by Prizes.vaultId
    var winnerAddress by Prizes.winnerAddress

    val vault by VaultEntity referencedOn Prizes.vaultId
}
