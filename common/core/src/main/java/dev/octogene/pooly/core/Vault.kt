package dev.octogene.pooly.core

import kotlinx.serialization.Serializable

@Serializable
data class Vault(
    val address: Address,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val network: ChainNetwork,
)
