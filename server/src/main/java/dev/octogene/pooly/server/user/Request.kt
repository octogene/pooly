package dev.octogene.pooly.server.user

import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserRequest(val username: String, val password: String, val email: String)

@Serializable
data class LoginUserRequest(val username: String, val password: String)
