package dev.octogene.pooly.server.config

import dev.octogene.pooly.server.cache.CacheType
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class AppConfig(
    val host: String,
    val port: Int,
    val metrics: Metrics,
    val database: DbConfig,
    val cache: CacheConfig,
    val security: SecurityConfig
)

@Serializable
data class Metrics(val minFreeMem: Int, val maxLoad: Double)

@Serializable
data class DbConfig(
    val host: String,
    val port: Int,
    val name: String,
    val driver: String,
    val username: String,
    val password: String
)

@Serializable
data class CacheConfig(
    val type: CacheType,
    val host: String,
    val port: Int,
    val defaultTTL: Duration,
    val cleanupInterval: Duration
)

@Serializable
data class SecurityConfig(
    val jwt: JwtConfig,
    val hashing: HashingConfig
)

@Serializable
data class HashingConfig(
    val argon2Type: String,
    val iterations: Int,
    val memory: Int,
    val parallelism: Int
)

@Serializable
data class JwtConfig(val secret: String)
