package dev.octogene.pooly.ptgraph.api.model

import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.Amount
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class IndexedPrize
@OptIn(ExperimentalTime::class)
constructor(
    val id: Int,
    val payout: Amount,
    val timestamp: Instant,
    val winner: String,
    val vault: String,
    val transactionHash: String,
)

fun IndexedPrize.toPrize(draw: IndexedPrize, vault: Vault): Prize = Prize(
    winner = Address.unsafeFrom(draw.winner),
    payout = draw.payout,
    transactionHash = draw.transactionHash,
    timestamp = draw.timestamp,
    vault = vault,
)
