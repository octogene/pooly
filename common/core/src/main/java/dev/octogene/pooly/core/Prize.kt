package dev.octogene.pooly.core

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

import java.math.BigInteger

@Serializable
data class Prize(
    @Serializable(with = BigIntegerSerializer::class)
    val payout: BigInteger,
    val timestamp: LocalDateTime,
    val winner: Address,
    val vault: Address
)
