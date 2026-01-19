package dev.octogene.pooly.server.user

import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.raise.ktor.server.response.Response
import arrow.raise.ktor.server.response.Response.Companion.Response
import dev.octogene.pooly.common.db.repository.UserRepository
import dev.octogene.pooly.common.db.repository.WalletRepository
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.server.model.mapToResponse
import dev.octogene.pooly.server.security.JwtGenerator
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized

class UserController(
    private val commonUserRepository: UserRepository,
    private val walletRepository: WalletRepository,
    private val jwtGenerator: JwtGenerator
) {
    context(_: Raise<Response>)
    suspend fun createUser(name: String, email: String, password: String): Response {
        return commonUserRepository.createUser(name, email, password)
            .map { Response(Created, "User $name created") }
            .mapLeft(::mapToResponse).bind()
    }

    context(_: Raise<Response>)
    suspend fun login(credential: UserCredential): Response {
        return commonUserRepository.findUserByUsername(credential.username).mapLeft {
            Response(Unauthorized, "Invalid username")
            // TODO: Pwd validation
        }.map {
            Response(OK, jwtGenerator.createToken(credential.username))
        }.bind()
    }

    context(_: Raise<Response>)
    suspend fun addWallets(username: String, rawAddresses: List<String>): Response {
        val user = commonUserRepository.findUserByUsername(username).mapLeft {
            Response(Unauthorized, "Unknown user, credentials must be outdated")
        }.bind()
        val addresses = rawAddresses.map { rawAddress ->
            Address.from(rawAddress).mapLeft(::mapToResponse).bind()
        }
        return walletRepository.addWallets(user.username, addresses)
            .map { Response(Created, "Wallets added") }
            .mapLeft(::mapToResponse).bind()
    }

    context(_: Raise<Response>)
    suspend fun removeWallets(username: String, rawAddresses: List<String>): Response {
        val user = commonUserRepository.findUserByUsername(username).mapLeft {
            Response(Unauthorized, "Unknown user, credentials must be outdated")
        }.bind()
        val addresses = rawAddresses.map { rawAddress ->
            Address.from(rawAddress).mapLeft(::mapToResponse).bind()
        }
        return walletRepository.removeWallets(user.username, addresses)
            .map { Response(OK, "Wallets removed") }
            .mapLeft(::mapToResponse).bind()
    }

    context(_: Raise<Response>)
    suspend fun getWallets(username: String): Response {
        val user = commonUserRepository.findUserByUsername(username).mapLeft {
            Response(Unauthorized, "Unknown user, credentials must be outdated")
        }.bind()
        return walletRepository.getWalletsForUser(user.username)
            .map { Response(OK, it) }
            .mapLeft(::mapToResponse).bind()
    }
}
