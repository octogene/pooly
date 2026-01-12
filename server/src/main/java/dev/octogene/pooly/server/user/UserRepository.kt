package dev.octogene.pooly.server.user

interface UserRepository {
    fun addWallet(name: String, address: String)
    fun removeWallet(address: String)
}