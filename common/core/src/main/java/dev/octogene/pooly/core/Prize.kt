package dev.octogene.pooly.core

import kotlinx.serialization.Serializable
import java.math.BigInteger
import kotlin.time.Instant

@Serializable
data class Prize(
    @Serializable(with = BigIntegerSerializer::class)
    val payout: BigInteger,
    val timestamp: Instant,
    val winner: Address,
    val vault: Vault,
    val transactionHash: String,
)
