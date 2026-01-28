package dev.octogene.pooly.server.cache

import arrow.core.Option
import dev.octogene.pooly.server.serialization.DynamicLookupSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer
import kotlin.time.Duration
import kotlin.time.Instant

interface CacheClient {
    suspend fun <T : Any> get(key: String, type: KSerializer<T>): Option<T>
    suspend fun <T : Any> set(key: String, value: T, ttl: Duration, type: KSerializer<T>)
    suspend fun <T : Any> set(key: String, value: T, expireAt: Instant, type: KSerializer<T>)
}

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
suspend inline fun <reified T : Any> CacheClient.get(key: String): Option<T> {
    val serializer = DynamicLookupSerializer() as KSerializer<T>
    return get(key, serializer)
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> CacheClient.set(key: String, value: T, expireAt: Instant) {
    val serializer = T::class.serializer()
    set(key, value, expireAt, serializer)
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified E : Any> CacheClient.set(key: String, values: List<E>, expireAt: Instant) {
    val serializer = ListSerializer(E::class.serializer())
    set(key, values, expireAt, serializer)
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> CacheClient.set(key: String, value: T, ttl: Duration) {
    val serializer = T::class.serializer()
    set(key, value, ttl, serializer)
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified E : Any> CacheClient.set(key: String, values: List<E>, ttl: Duration) {
    val serializer = ListSerializer(E::class.serializer())
    set(key, values, ttl, serializer)
}

