package dev.octogene.pooly.rpc

import java.math.BigInteger

data class RpcVault(
    val address: String,
    val name: String,
    val symbol: String,
    val decimals: BigInteger
)
