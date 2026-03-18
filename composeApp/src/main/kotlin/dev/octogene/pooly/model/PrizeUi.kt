package dev.octogene.pooly.model

import dev.octogene.pooly.core.Prize
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

data class PrizeUi(
    val id: String,
    val timestamp: Instant,
    val formattedPayout: String,
    val vaultSymbol: String?,
    val date: LocalDate,
)

fun Prize.toUiModel(): PrizeUi {
    val formattedPayout = payout.format(vault.decimals)
    val date = timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date

    return PrizeUi(
        id = "${this.winner.value}-${this.transactionHash}-${this.vault.address}",
        timestamp = timestamp,
        formattedPayout = formattedPayout,
        vaultSymbol = vault.symbol,
        date = date,
    )
}
