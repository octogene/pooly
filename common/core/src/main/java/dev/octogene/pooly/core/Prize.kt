package dev.octogene.pooly.core

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Prize(
    @Serializable(with = AmountSerializer::class)
    val payout: Amount,
    val timestamp: Instant,
    val winner: Address,
    val vault: Vault,
    val transactionHash: String,
)
