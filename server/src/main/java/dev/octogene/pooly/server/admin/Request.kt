package dev.octogene.pooly.server.admin

import kotlinx.serialization.Serializable

@Serializable
data class ClearCacheRequest(val pattern: String)
