package dev.octogene.pooly.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class AmountSerializer : KSerializer<Amount> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Amount", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Amount) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Amount = Amount.from(decoder.decodeString())
}
