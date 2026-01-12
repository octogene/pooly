package dev.octogene.pooly.ptgraph.api.model

import java.math.BigInteger
import kotlin.jvm.JvmInline

@JvmInline
internal value class Amount(val value: BigInteger) {
    fun from(raw: String): Amount {
        return Amount(BigInteger(raw))
    }
}
