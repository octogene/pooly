package dev.octogene.pooly.core

import java.math.BigInteger

@JvmInline
value class Amount(private val value: BigInteger) {
    companion object {
        fun from(raw: String): Amount = Amount(BigInteger(raw))
    }

    fun toLong(): Long = value.toLong()

    fun format(decimals: Int): String = value.toBigDecimal().movePointLeft(decimals).toPlainString()

    override fun toString(): String = value.toString()
}
