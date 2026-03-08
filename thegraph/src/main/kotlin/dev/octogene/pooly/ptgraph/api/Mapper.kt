package dev.octogene.pooly.ptgraph.api

import dev.octogene.pooly.core.Amount
import dev.octogene.pooly.ptgraph.api.model.IndexedPrize
import dev.octogene.pooly.thegraph.DrawsByAddressesQuery
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun DrawsByAddressesQuery.PrizeClaim.toGraphDraw(): IndexedPrize? {
    val instant = timestamp.toInstant()

    return instant?.let {
        IndexedPrize(
            id = draw.drawId,
            payout = Amount.from(payout),
            timestamp = it,
            winner = winner,
            vault = prizeVault.id,
            transactionHash = draw.txHash,
        )
    }
}

@OptIn(ExperimentalTime::class)
private fun String.toInstant(): Instant? = this.toLongOrNull()?.let { Instant.fromEpochSeconds(it) }
