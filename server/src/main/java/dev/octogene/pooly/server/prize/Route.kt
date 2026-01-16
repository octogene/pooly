package dev.octogene.pooly.server.prize

import arrow.raise.ktor.server.routing.getOrRaise
import dev.octogene.pooly.server.user.getUsername
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

fun Route.prizesRoute() {
    val prizeController: PrizeController by inject()

    authenticate("auth-jwt") {
        getOrRaise("/prizes") {
            val username = getUsername()
            prizeController.getAllPrizes(username)
        }
    }
}