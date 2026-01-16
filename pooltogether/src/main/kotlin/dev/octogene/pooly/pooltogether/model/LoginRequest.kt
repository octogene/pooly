package dev.octogene.pooly.pooltogether.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val expiration: Instant
)
