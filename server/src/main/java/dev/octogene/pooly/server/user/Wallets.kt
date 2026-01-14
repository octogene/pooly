package dev.octogene.pooly.server.user

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Wallets(val content: List<String>)
