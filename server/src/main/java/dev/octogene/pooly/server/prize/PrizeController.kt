package dev.octogene.pooly.server.prize

import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.raise.ktor.server.response.Response
import dev.octogene.pooly.server.model.mapToResponse
import dev.octogene.pooly.server.user.UserRepository
import io.ktor.http.HttpStatusCode

class PrizeController(
    private val prizeRepository: PrizeRepository,
    private val userRepository: UserRepository
) {
    context(_: Raise<Response>)
    suspend fun getAllPrizes(username: String): Response {
        val wallets = userRepository.getWallets(username).mapLeft {
            Response.Response(HttpStatusCode.Forbidden)
        }.bind()
        return prizeRepository.getAllPrizes(wallets)
            .map { Response.Response(HttpStatusCode.OK, it) }
            .mapLeft(::mapToResponse).bind()
    }
}