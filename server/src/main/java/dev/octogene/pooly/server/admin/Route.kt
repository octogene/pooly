package dev.octogene.pooly.server.admin

import arrow.raise.ktor.server.routing.postOrRaise
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.adminRoutes() {
    val adminController: AdminController by inject()

    authenticate("auth-admin") {
        route("/admin") {
            postOrRaise("/cache/clear") {
                val request = call.receive<ClearCacheRequest>()
                adminController.clearCache(request)
            }
        }
    }
}