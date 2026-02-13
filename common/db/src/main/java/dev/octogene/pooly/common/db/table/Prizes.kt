package dev.octogene.pooly.common.db.table

import dev.octogene.pooly.common.db.ADDRESS_LENGTH
import dev.octogene.pooly.common.db.MAX_AMOUNT_LENGTH
import dev.octogene.pooly.common.db.TX_HASH_LENGTH
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp

object Prizes : LongIdTable("prizes") {
    val amount = varchar("amount", MAX_AMOUNT_LENGTH)
    val network = enumeration<ChainNetwork>("network").index()
    val timestamp = timestamp("timestamp").index()
    val transactionHash = varchar("transaction_hash", TX_HASH_LENGTH)
    val vaultId = varchar("vault_id", ADDRESS_LENGTH).references(Vaults.id).index()
    val winnerAddress = varchar("winner_address", ADDRESS_LENGTH).index()

    init {
        uniqueIndex(vaultId, transactionHash, winnerAddress)
    }
}

class PrizeEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<PrizeEntity>(Prizes)

    var amount by Prizes.amount.memoizedTransform(
        unwrap = { it.toString() },
        wrap = { it.toBigInteger() },
    )
    var network by Prizes.network
    var timestamp by Prizes.timestamp
    var transactionHash by Prizes.transactionHash
    var vaultId by Prizes.vaultId
    var winnerAddress by Prizes.winnerAddress.transform(
        unwrap = { it.value },
        wrap = { Address.unsafeFrom(it) },
    )

    val vault by VaultEntity referencedOn Prizes.vaultId
}

internal fun PrizeEntity.toPrize(): Prize = Prize(
    payout = amount,
    timestamp = timestamp,
    winner = winnerAddress,
    transactionHash = transactionHash,
    vault = Vault(
        address = Address.unsafeFrom(vault.id.value),
        name = vault.name,
        symbol = vault.tokenSymbol,
        decimals = vault.tokenDecimals,
        network = network,
    ),
)
