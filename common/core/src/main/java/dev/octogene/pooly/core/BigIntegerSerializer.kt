package dev.octogene.pooly.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import java.math.BigInteger
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class BigIntegerSerializer: KSerializer<BigInteger> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("BigInteger", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigInteger) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): BigInteger {
        return BigInteger(decoder.decodeString())
    }
}