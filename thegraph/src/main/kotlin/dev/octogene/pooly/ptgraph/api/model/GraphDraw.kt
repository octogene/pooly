package dev.octogene.pooly.ptgraph.api.model

import kotlinx.datetime.LocalDateTime
import java.math.BigInteger
import kotlin.time.ExperimentalTime

data class GraphDraw
@OptIn(ExperimentalTime::class)
constructor(
    val id: Int,
    val payout: BigInteger,
    val timestamp: LocalDateTime,
    val winner: String,
    val vault: String
)
