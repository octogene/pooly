package dev.octogene.pooly.server.cache

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.raise.either
import arrow.core.right
import arrow.core.toOption
import dev.octogene.pooly.server.serialization.DynamicLookupSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class InMemoryCacheClient(
    private val cache: MutableMap<String, String> = mutableMapOf(),
    private val cacheTTL: MutableMap<String, Instant> = mutableMapOf(),
    private val json: Json = Json,
    private val defaultTTL: Duration = 2.minutes,
    private val cleanupInterval: Duration = 10.minutes,
    private val logger: Logger = LoggerFactory.getLogger(InMemoryCacheClient::class.java)
) : CacheClient {
    suspend fun runBackgroundCleanup() = withContext(Dispatchers.IO) {
        logger.info("Starting background cleanup")
        while (isActive) {
            delay(cleanupInterval)
            val now = Clock.System.now()
            val expiredKeys = cacheTTL.filterValues { expireAt -> expireAt <= now }.keys
            expiredKeys.forEach {
                cache.remove(it)
                cacheTTL.remove(it)
            }
            logger.debug("Cleaned up {} expired keys", expiredKeys.size)
        }
    }

    override suspend fun <T : Any> get(
        key: String,
        type: KSerializer<T>
    ): Option<T> {
        logger.debug("Getting value for key {} with type {}", key, type.descriptor.serialName)
        val jsonString = cache[key] ?: return None

        return try {
            val decodedValue = json.decodeFromString(type, jsonString)
            decodedValue.toOption()
        } catch (e: Exception) {
            logger.error(
                "Error deserializing value for key '{}' to type {}: {}",
                key,
                type.descriptor.serialName,
                e.message
            )
            None
        }
    }

    override suspend fun <T : Any> set(
        key: String,
        value: T,
        ttl: Duration,
        type: KSerializer<T>
    ) {
        set(key, value, Clock.System.now().plus(ttl), type)
    }

    override suspend fun <T : Any> set(
        key: String,
        value: T,
        expireAt: Instant,
        type: KSerializer<T>
    ) {
        val jsonString = try {
            json.encodeToString(type, value)
        } catch (e: SerializationException) { // Catching the specific exception
            logger.error(
                "Error serializing value for key {} with type {}: {}",
                key,
                value::class.simpleName,
                e.message
            )
            return
        } catch (e: Exception) {
            logger.error("Unexpected error during serialization for key {}: {}", key, e.message)
            return
        }
        cache[key] = jsonString
        cacheTTL[key] = expireAt
    }

    override suspend fun clearByPattern(pattern: String): Either<Throwable, Long> = either {
        val filter = pattern.toRegex()
        val keysToDelete = cache.keys.filter { filter.matches(it) }
        keysToDelete.forEach { key ->
            cache.remove(key)
        }
        keysToDelete.size.toLong()
    }
}