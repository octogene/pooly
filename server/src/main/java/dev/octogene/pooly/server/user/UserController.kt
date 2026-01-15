package dev.octogene.pooly.server.user

import arrow.core.Either
import dev.octogene.pooly.server.model.DatabaseError

class UserController(
    private val userRepository: UserRepository
) {
    fun createUser(name: String, email: String, password: String) {
        userRepository.createUser(name, email, password)
    }

    fun addWallets(username: String, addresses: List<String>): Either<DatabaseError, Unit> {
        return userRepository.addWallets(username, addresses)
    }

    fun removeWallets(username: String, addresses: List<String>) {
        userRepository.removeWallets(username, addresses)
    }

    fun getWallets(username: String): Either<DatabaseError, List<String>> {
        return userRepository.getWallets(username)
    }
}
