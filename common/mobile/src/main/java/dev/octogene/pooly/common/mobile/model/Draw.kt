package dev.octogene.pooly.common.mobile.model

import kotlinx.datetime.LocalDateTime
import java.math.BigInteger
import kotlin.time.ExperimentalTime

data class Draw
@OptIn(ExperimentalTime::class)
constructor(
    val id: Int,
    val payout: BigInteger,
    val timestamp: LocalDateTime,
    val winner: String,
    val vault: Vault
)
