@file:UseSerializers(
    NonEmptyListSerializer::class,
)
package dev.octogene.pooly.server.user

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
@JvmInline
value class Wallets(val content: NonEmptyList<String>)
