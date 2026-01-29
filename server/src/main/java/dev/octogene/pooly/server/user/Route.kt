package dev.octogene.pooly.server.user

import arrow.core.raise.context.bind
import arrow.raise.ktor.server.routing.deleteOrRaise
import arrow.raise.ktor.server.routing.getOrRaise
import arrow.raise.ktor.server.routing.postOrRaise
import dev.octogene.pooly.server.receiveOrRaise
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import org.koin.ktor.ext.inject

fun Route.usersRoute() {
    val userController: UserController by inject()

    postOrRaise("/register") {
        val user = call.receiveOrRaise<RegisterUserRequest>("Invalid user").bind()
        userController.createUser(user.username, user.email, user.password)
    }

    postOrRaise("/login") {
        val credentials = call.receiveOrRaise<LoginUserRequest>("Invalid credentials").bind()
        userController.login(credentials)
    }

    authenticate("auth-jwt") {
        getOrRaise("/wallets") {
            val username = getUsername()
            userController.getWallets(username)
        }

        postOrRaise("/wallets") {
            val username = getUsername()
            val wallets = call.receiveOrRaise<Wallets>("Invalid wallets").bind()
            userController.addWallets(username, wallets.content)
        }

        deleteOrRaise {
            val username = getUsername()
            val wallets = call.receiveOrRaise<Wallets>("Invalid wallets").bind()
            userController.removeWallets(username, wallets.content)
        }
    }
}

fun RoutingContext.getUsername(): String {
    val principal = call.principal<JWTPrincipal>()
    return principal!!.payload.getClaim("username").asString()
}
