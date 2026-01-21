package dev.octogene.pooly.server.prize

import arrow.raise.ktor.server.routing.getOrRaise
import dev.octogene.pooly.server.user.getUsername
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

fun Route.prizesRoute() {
    val prizeController: PrizeController by inject()

    authenticate("auth-jwt") {
        getOrRaise("/prizes") {
            val username = getUsername()
            val page = call.queryParameters["page"]?.toInt()
            val pageSize = call.queryParameters["pagesSize"]?.toInt()
            if (page != null) {
                application.log.debug("Getting prizes by page {} with page size {}", page, pageSize)
                prizeController.getAllPrizesByPage(username, page, pageSize)
            } else prizeController.getAllPrizes(username)
        }
    }
}