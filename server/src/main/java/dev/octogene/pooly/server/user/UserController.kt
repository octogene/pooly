package dev.octogene.pooly.server.user

class UserController(
    private val userRepository: UserRepository
) {
    fun createUser(name: String, email: String, password: String) {
        userRepository.createUser(name, email, password)
    }

    fun addWallets(username: String, addresses: List<String>) {
        userRepository.addWallets(username, addresses)
    }

    fun removeWallets(username: String, addresses: List<String>) {
        userRepository.removeWallets(username, addresses)
    }

    fun getWallets(username: String): List<String> {
        return userRepository.getWallets(username)
    }
}
