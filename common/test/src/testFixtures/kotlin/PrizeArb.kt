import dev.octogene.pooly.core.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import kotlin.time.Instant

/**
 * Property generator for [dev.octogene.pooly.core.Address].
 */
fun Arb.Companion.address(): Arb<Address> =
    Arb.string(40, Codepoint.hex()).map { Address.unsafeFrom("0x$it") }

/**
 * Property generator for [dev.octogene.pooly.core.Amount].
 */
fun Arb.Companion.amount(): Arb<Amount> =
    Arb.bigInt(maxNumBits = 256).map { Amount.from(it.abs().toString()) }

/**
 * Property generator for [dev.octogene.pooly.core.Vault].
 */
fun Arb.Companion.vault(): Arb<Vault> = arbitrary {
    Vault(
        address = Arb.address().bind(),
        name = Arb.string(minSize = 5, maxSize = 20).bind(),
        symbol = Arb.string(minSize = 3, maxSize = 5).bind(),
        decimals = Arb.int(0, 18).bind(),
        network = Arb.enum<ChainNetwork>().bind(),
    )
}

/**
 * Property generator for [dev.octogene.pooly.core.Prize].
 */
fun Arb.Companion.prize(): Arb<Prize> = arbitrary {
    Prize(
        payout = Arb.amount().bind(),
        timestamp = Instant.fromEpochSeconds(Arb.long(0, 2000000000).bind()),
        winner = Arb.address().bind(),
        vault = Arb.vault().bind(),
        transactionHash = "0x" + Arb.string(64, Codepoint.hex()).bind(),
    )
}

val prizeArb = Arb.prize()
