package dev.octogene.pooly.server.admin

import arrow.core.raise.context.bind
import arrow.raise.ktor.server.response.Response.Companion.Response
import arrow.raise.ktor.server.routing.deleteOrRaise
import arrow.raise.ktor.server.routing.postOrRaise
import dev.octogene.pooly.server.receiveOrRaise
import dev.octogene.pooly.server.user.RegisterUserRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.adminRoutes() {
    val adminController: AdminController by inject()

    authenticate("auth-admin", "auth-admin-jwt") {
        route("/admin") {
            deleteOrRaise("/cache") {
                val request = call.receiveOrRaise<ClearCacheRequest>("Invalid request").bind()
                adminController.clearCache(request)
            }

            route("/users") {
                postOrRaise {
                    val request = call.receiveOrRaise<RegisterUserRequest>("Invalid request").bind()
                    adminController.createAdmin(
                        request.username,
                        request.password,
                        request.email
                    )
                }

                deleteOrRaise("/{username}") {
                    val username = call.pathParameters["username"]
                    if (username == null) {
                        Response(HttpStatusCode.BadRequest)
                    } else adminController.removeUser(username)
                }
            }
        }
    }
}
