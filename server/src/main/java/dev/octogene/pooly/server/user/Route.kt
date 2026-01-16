package dev.octogene.pooly.server.user

import arrow.raise.ktor.server.response.Response
import arrow.raise.ktor.server.routing.deleteOrRaise
import arrow.raise.ktor.server.routing.getOrRaise
import arrow.raise.ktor.server.routing.postOrRaise
import dev.octogene.pooly.server.security.JwtGenerator
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import org.koin.ktor.ext.inject

fun Route.usersRoute() {
    val userController: UserController by inject()
    val jwtGenerator: JwtGenerator by inject()

    postOrRaise("/register") {
        val user = call.receive<User>()
        userController.createUser(user.username, user.email, user.password)
    }

    postOrRaise("/login") {
        val user = call.receive<UserCredential>()
        // TODO: Proper validation & jwt config
        Response.Response(HttpStatusCode.OK, jwtGenerator.createToken(user.username))
    }

    authenticate("auth-jwt") {
        getOrRaise("/wallets") {
            val username = getUsername()
            userController.getWallets(username)
        }

        postOrRaise("/wallets") {
            val username = getUsername()
            val wallets = call.receive<Wallets>()
            userController.addWallets(username, wallets.content)
        }

        deleteOrRaise {
            val username = getUsername()
            val wallets = call.receive<Wallets>()
            userController.removeWallets(username, wallets.content)
        }
    }
}

fun RoutingContext.getUsername(): String {
    val principal = call.principal<JWTPrincipal>()
    return principal!!.payload.getClaim("username").asString()
}
