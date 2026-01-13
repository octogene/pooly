package dev.octogene.pooly.core

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.context.ensure
import arrow.core.raise.either
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Address private constructor(val value: String) {

    companion object {
        private const val ADDRESS_LENGTH = 42

        fun unsafeFrom(rawAddress: String): Address = from(rawAddress).getOrElse {
            throw IllegalArgumentException("Invalid address: $rawAddress")
        }

        fun from(rawAddress: String): Either<InvalidField, Address> = either {
            ensure(rawAddress.isNotBlank()) {
                InvalidField("address", "Address cannot be empty")
            }
            ensure(rawAddress.startsWith("0x") && rawAddress.length == ADDRESS_LENGTH) {
                InvalidField(
                    "address",
                    "Invalid address length"
                )
            }
            Address(rawAddress)
        }
    }
}
