package dev.octogene.pooly.server.config

import dev.octogene.pooly.common.cache.config.CacheConfig
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class AppConfig(
    val host: String,
    val port: Int,
    val metrics: Metrics,
    val database: DbConfig,
    val cache: CacheConfig,
    val security: SecurityConfig,
    val loglevel: String,
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
    val password: String,
)

@Serializable
data class SecurityConfig(val jwt: JwtConfig, val hashing: HashingConfig, val apikey: String)

@Serializable
data class HashingConfig(val argon2Type: String, val iterations: Int, val memory: Int, val parallelism: Int)

@Serializable
data class JwtConfig(val secret: String, val issuer: String, val audience: String, val expiration: Duration)
