package dev.octogene.pooly.core

data class User(
    val username: String,
    val email: String?,
    val passwordHash: String
)

data class UserWithWallets(
    val user: User,
    val wallets: List<Address>
)
