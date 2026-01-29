package dev.octogene.pooly.server.cache

import arrow.core.Option
import arrow.core.none
import arrow.core.toOption
import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.serde.get
import eu.vendeli.rethis.command.serde.set
import eu.vendeli.rethis.shared.request.string.SetExpire
import kotlinx.serialization.KSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ValkeyCacheClient(
    private val client: ReThis,
    val defaultTTL: Duration = 10.minutes,
    val logger: Logger = LoggerFactory.getLogger(ValkeyCacheClient::class.java)
) : CacheClient {

    override suspend fun <T : Any> get(key: String, serializer: KSerializer<T>): Option<T> {
        logger.debug("Getting value for key {}", key)
        return try {
            client.get(key = key, serializer).toOption()
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

    override suspend fun <T : Any> set(
        key: String,
        value: T,
        expireAt: Instant,
        type: KSerializer<T>
    ) {
        logger.debug("Setting value for key {} (expireAt: {})", key, expireAt)
        try {
            client.set(
                key = key,
                value = value,
                serializer = type,
                options = arrayOf(
                    SetExpire.ExAt(expireAt),
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to set $key : {}", e.message)
        }
    }
}