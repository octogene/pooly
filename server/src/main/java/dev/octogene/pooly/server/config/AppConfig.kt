package dev.octogene.pooly.server.config

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(val host: String, val port: Int, val metrics: Metrics, val database: DbConfig)

@Serializable
data class Metrics(val minFreeMem: Int, val maxLoad: Double)

@Serializable
data class DbConfig(val url: String, val driver: String, val user: String, val password: String)
