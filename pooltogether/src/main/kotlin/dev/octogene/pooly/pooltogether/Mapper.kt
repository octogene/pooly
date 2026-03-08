package dev.octogene.pooly.pooltogether

import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.Vault
import dev.octogene.pooly.pooltogether.db.GetAllDraws

fun GetAllDraws.toVault(): Vault = Vault(
    address = Address.unsafeFrom(vaultAddress),
    name = name,
    symbol = symbol,
    decimals = decimals.toInt(),
    network = network,
)
