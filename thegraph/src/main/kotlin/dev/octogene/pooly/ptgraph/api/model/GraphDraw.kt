package dev.octogene.pooly.ptgraph.api.model

import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.math.BigInteger
import kotlin.time.ExperimentalTime

data class GraphDraw
@OptIn(ExperimentalTime::class)
constructor(
    val id: Int,
    val payout: BigInteger,
    val timestamp: LocalDateTime,
    val winner: String,
    val vault: String,
    val transactionHash: String
)

fun GraphDraw.toPrize(
    draw: GraphDraw,
    vault: Vault,
    timeZone: TimeZone = TimeZone.UTC
): Prize = Prize(
    winner = Address.unsafeFrom(draw.winner),
    payout = draw.payout,
    transactionHash = draw.transactionHash,
    timestamp = draw.timestamp.toInstant(timeZone),
    vault = vault
)
