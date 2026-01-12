package dev.octogene.pooly.core

import arrow.core.Either
import arrow.core.raise.context.ensure
import arrow.core.raise.either
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Address private constructor(val value: String) {

    companion object {
        fun from(rawAddress: String): Either<InvalidField, Address> = either {
            ensure(rawAddress.isNotBlank()) {
                InvalidField("address", "Address cannot be empty")
            }
            ensure(rawAddress.startsWith("0x") && rawAddress.length == 42) {
                InvalidField(
                    "address",
                    "Invalid address length"
                )
            }
            Address(rawAddress)
        }
    }
}