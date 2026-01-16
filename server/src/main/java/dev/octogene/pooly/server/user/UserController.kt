package dev.octogene.pooly.server.user

import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.raise.ktor.server.response.Response
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.server.model.mapToResponse
import io.ktor.http.HttpStatusCode

class UserController(
    private val userRepository: UserRepository
) {
    context(_: Raise<Response>)
    fun createUser(name: String, email: String, password: String): Response {
        return userRepository.createUser(name, email, password)
            .map { Response.Response(HttpStatusCode.Created, "User $name created") }
            .mapLeft(::mapToResponse).bind()
    }

    context(_: Raise<Response>)
    fun addWallets(username: String, rawAddresses: List<String>): Response {
        val addresses = rawAddresses.map { rawAddress ->
            Address.from(rawAddress).mapLeft(::mapToResponse).bind()
        }
        return userRepository.addWallets(username, addresses)
            .map { Response.Response(HttpStatusCode.Created, "Wallets added") }
            .mapLeft(::mapToResponse).bind()
    }

    context(_: Raise<Response>)
    fun removeWallets(username: String, rawAddresses: List<String>): Response {
        val addresses = rawAddresses.map { rawAddress ->
            Address.from(rawAddress).mapLeft(::mapToResponse).bind()
        }
        return userRepository.removeWallets(username, addresses)
            .map { Response.Response(HttpStatusCode.OK, "Wallets removed") }
            .mapLeft(::mapToResponse).bind()
    }

    context(_: Raise<Response>)
    fun getWallets(username: String): Response {
        return userRepository.getWallets(username)
            .map { Response.Response(HttpStatusCode.OK, it) }
            .mapLeft(::mapToResponse).bind()
    }
}
