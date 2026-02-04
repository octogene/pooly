package dev.octogene.pooly.common.cache

import arrow.core.Either
import arrow.core.Option
import arrow.core.left
import arrow.core.none
import arrow.core.raise.either
import arrow.core.right
import arrow.core.toOption
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.SetArgs
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.time.toJavaInstant

internal class ValkeyCacheClient(
    private val lettuceClient: RedisClient,
    private val json: Json,
    val defaultTTL: Duration = 10.minutes,
    val logger: Logger = LoggerFactory.getLogger(ValkeyCacheClient::class.java)
) : CacheClient {

    private val connection = lettuceClient.connect()

    override suspend fun <T : Any> get(key: String, serializer: KSerializer<T>): Option<T> {
        logger.debug("Getting value for key {}", key)
        return try {
            connection.sync().get(key)?.let { data ->
                logger.debug("Getting string of ${data.length} length")
                json.decodeFromString(deserializer = serializer, data)
            }.toOption()
        } catch (e: Exception) {
            logger.error("Failed to fetch $key: {}", e.message)
            none()
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

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    override suspend fun <T : Any> set(
        key: String,
        value: T,
        expireAt: Instant,
        type: KSerializer<T>
    ) {
        logger.debug("Setting value for key {} (expireAt: {})", key, expireAt)
        try {
            connection.sync().set(
                key,
                json.encodeToString(serializer = type, value),
                SetArgs.Builder.exAt(expireAt.toJavaInstant())
            )
        } catch (e: Exception) {
            logger.error("Failed to set $key : {}", e.message)
        }
    }

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    // TODO: Improve arrow usage
    override suspend fun clearByPattern(pattern: String): Either<Throwable, Long> = either {
        return try {
            val keys = connection.sync().keys(pattern)
            keys?.let { keys ->
                logger.debug("Clearing ${keys?.size ?: 0} keys")
                connection.sync().del(*keys.toTypedArray())
            }
            keys.size.toLong().right()
        } catch (e: Throwable) {
            logger.error("Failed to clear cache : {}", e.message)
            e.left()
        }
    }
}