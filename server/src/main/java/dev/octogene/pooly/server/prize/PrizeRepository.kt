package dev.octogene.pooly.server.prize

import dev.octogene.pooly.common.db.table.PrizeEntity
import dev.octogene.pooly.common.db.table.Prizes
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.math.BigInteger

interface PrizeRepository {
    fun getAllPrizes(wallets: List<Address>): List<Prize>
    fun getLatestPrizes(wallets: List<Address>): List<Prize>
}

class PrizeRepositoryImpl(
    private val database: Database
) : PrizeRepository {
    override fun getLatestPrizes(wallets: List<Address>): List<Prize> {
        return transaction(database) {
            val latestTimestampForWallet = Prizes
                .select(Prizes.winnerAddress, Prizes.timestamp.max())
                .where { Prizes.winnerAddress inList wallets.map { it.value } }
                .groupBy(Prizes.winnerAddress)

            // TODO: Finish latest prizes transaction

            emptyList()
        }
    }

    override fun getAllPrizes(wallets: List<Address>): List<Prize> {
        val prizes = transaction(database) {
            Prizes
                .selectAll()
                .where { Prizes.winnerAddress inList wallets.map { it.value } }
                .map {
                    with(PrizeEntity.wrapRow(it)) {
                        Prize(
                            // TODO: Should keep payout as String, BigInteger only useful on frontend
                            payout = BigInteger(amount),
                            timestamp = timestamp,
                            winner = Address.unsafeFrom(winnerAddress),
                            vault = Vault(
                                Address.unsafeFrom(vault.id.value),
                                vault.name,
                                vault.tokenSymbol,
                                vault.tokenDecimals,
                                ChainNetwork.BASE
                            )
                        )
                    }
                }
        }
        return prizes
    }
}
