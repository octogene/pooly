package dev.octogene.pooly.common.cache.config

import dev.octogene.pooly.common.cache.CacheType
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class CacheConfig(
    val type: CacheType,
    val host: String,
    val port: Int,
    val defaultTTL: Duration,
    val cleanupInterval: Duration
)