package dev.octogene.pooly.core

data class User(val username: String, val email: String?, val passwordHash: PasswordHash, val role: UserRole)

data class UserWithWallets(val user: User, val wallets: List<Address>)

enum class UserRole {
    USER,
    ADMIN,
}
