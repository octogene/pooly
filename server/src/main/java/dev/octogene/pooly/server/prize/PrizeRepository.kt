package dev.octogene.pooly.server.prize

import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.Prize

interface PrizeRepository {
    fun getLatestPrizes(wallets: List<Address>): List<Prize>
    fun getAllPrizes(wallets: List<Address>): List<Prize>
}
