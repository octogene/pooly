package dev.octogene.pooly.server.cache

import arrow.core.Option
import arrow.core.toOption
import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.serde.get
import eu.vendeli.rethis.command.serde.set
import eu.vendeli.rethis.shared.request.string.SetExpire
import kotlinx.serialization.KSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ValkeyCacheClient(
    val client: ReThis,
    val defaultTTL: Duration = 10.minutes,
    val logger: Logger = LoggerFactory.getLogger(ValkeyCacheClient::class.java)
) : CacheClient {

    override suspend fun <T : Any> get(key: String, serializer: KSerializer<T>): Option<T> {
        logger.debug("Getting value for key {}", key)
        return client.get(key = key, serializer).toOption()
    }

    override suspend fun set(key: String, value: Any) {
        set(key, value, defaultTTL)
    }

    override suspend fun set(key: String, value: Any, ttl: Duration) {
        logger.debug("Setting value for key {} (TTL: {})", key, ttl)
        client.set(
            key = key,
            value = value,
            options = arrayOf(
                SetExpire.Ex(ttl),
            )
        )
    }

    override suspend fun set(key: String, value: Any, expireAt: Instant) {
        logger.debug("Setting value for key {} (expireAt: {})", key, expireAt)
        client.set(
            key = key,
            value = value,
            options = arrayOf(
                SetExpire.ExAt(expireAt),
            )
        )
    }
}