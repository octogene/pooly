package dev.octogene.pooly.server.admin

import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.raise.ktor.server.response.Response
import arrow.raise.ktor.server.response.Response.Companion.Response
import dev.octogene.pooly.common.cache.CacheClient
import dev.octogene.pooly.core.UserRole
import dev.octogene.pooly.server.model.mapToResponse
import dev.octogene.pooly.server.security.AuthenticationService
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.Accepted
import io.ktor.http.HttpStatusCode.Companion.Created
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AdminController(
    private val cacheClient: CacheClient,
    private val authenticationService: AuthenticationService,
    private val logger: Logger = LoggerFactory.getLogger(AdminController::class.java),
) {
    context(_: Raise<Response>)
    suspend fun clearCache(request: ClearCacheRequest): Response {
        cacheClient.clearByPattern(request.pattern).mapLeft {
            Response(HttpStatusCode.InternalServerError)
        }
        return Response(HttpStatusCode.OK)
    }

    context(_: Raise<Response>)
    suspend fun createAdmin(name: String, email: String, password: String): Response =
        authenticationService.register(name, email, password, UserRole.ADMIN)
            .map {
                logger.warn("Created admin {}", name)
                Response(Created, "Admin $name created")
            }
            .mapLeft {
                logger.error("Error creating admin {} : {}", name, it)
                mapToResponse(it)
            }.bind()

    context(_: Raise<Response>)
    suspend fun removeUser(username: String): Response = authenticationService.removeUser(username)
        .map { Response(Accepted, "$username removed") }
        .mapLeft {
            logger.error("Error removing user {} : {}", username, it)
            mapToResponse(it)
        }.bind()
}
