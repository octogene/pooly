package dev.octogene.pooly.core

import kotlinx.serialization.Serializable
import java.math.BigInteger

@Serializable
data class Vault(
    val address: Address,
    val name: String,
    val symbol: String,
    @Serializable(with = BigIntegerSerializer::class)
    val decimals: BigInteger
)
