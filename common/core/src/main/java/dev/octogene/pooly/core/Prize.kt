package dev.octogene.pooly.core

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.math.BigInteger

@Serializable
data class Prize(
    val payout: BigInteger,
    val timestamp: LocalDateTime,
    val winner: Address,
    val vault: Address
)
