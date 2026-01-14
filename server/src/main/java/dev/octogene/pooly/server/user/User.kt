package dev.octogene.pooly.server.user

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    val password: String,
    val email: String
)

@Serializable
data class UserCredential(
    val username: String,
    val password: String
)
