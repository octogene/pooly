package dev.octogene.pooly.worker.model

import kotlin.time.Duration

data class AppConfig(val alchemyKey: String, val database: DbConfig, val checkInterval: Duration)

data class DbConfig(
    val host: String,
    val port: Int,
    val name: String,
    val driver: String,
    val username: String,
    val password: String,
)
