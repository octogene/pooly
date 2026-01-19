package dev.octogene.pooly.server.prize

import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.raise.ktor.server.response.Response
import arrow.raise.ktor.server.response.Response.Companion.Response
import dev.octogene.pooly.common.db.repository.UserRepository
import dev.octogene.pooly.common.db.repository.PrizeRepository
import dev.octogene.pooly.common.db.repository.WalletRepository
import dev.octogene.pooly.server.model.mapToResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.Unauthorized

class PrizeController(
    private val prizeRepository: PrizeRepository,
    private val walletRepository: WalletRepository,
    private val userRepository: UserRepository
) {
    context(_: Raise<Response>)
    suspend fun getAllPrizes(username: String): Response {
        val user = userRepository.findUserByUsername(username).mapLeft {
            Response(Unauthorized, "Unknown user, credentials must be outdated")
        }.bind()
        val wallets = walletRepository.getWalletsForUser(user.username).mapLeft {
            Response(HttpStatusCode.Forbidden)
        }.bind()
        return prizeRepository.getAllPrizes(wallets)
            .map { Response(HttpStatusCode.OK, it) }
            .mapLeft(::mapToResponse).bind()
    }
}