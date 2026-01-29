package dev.octogene.pooly.server

import arrow.core.Either
import dev.octogene.pooly.server.model.mapToResponse
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive

suspend inline fun <reified T: Any> ApplicationCall.receiveOrRaise(message: String) = Either.catch {
    receive<T>()
}.mapLeft {
    mapToResponse(it, message)
}
