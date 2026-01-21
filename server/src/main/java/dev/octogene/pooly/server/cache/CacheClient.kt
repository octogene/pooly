package dev.octogene.pooly.server.cache

import arrow.core.Option
import kotlinx.serialization.KSerializer
import kotlin.time.Duration
import kotlin.time.Instant

interface CacheClient {
    suspend fun <T : Any> get(key: String, type: KSerializer<T>): Option<T>
    suspend fun set(key: String, value: Any)
    suspend fun set(key: String, value: Any, ttl: Duration)
    suspend fun set(key: String, value: Any, expireAt: Instant)
}

