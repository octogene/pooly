package dev.octogene.pooly.worker.model

import kotlin.time.Duration

data class AppConfig(
    val db: DbConfig,
    val checkInterval: Duration,
)

data class DbConfig(
    val url: String,
    val driver: String,
    val user: String,
    val password: String,
)
