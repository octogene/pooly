package dev.octogene.pooly.server.user

import arrow.core.raise.context.bind
import arrow.raise.ktor.server.routing.deleteOrRaise
import arrow.raise.ktor.server.routing.getOrRaise
import arrow.raise.ktor.server.routing.patchOrRaise
import arrow.raise.ktor.server.routing.postOrRaise
import dev.octogene.pooly.core.UserRole
import dev.octogene.pooly.server.receiveOrRaise
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.route
import io.ktor.utils.io.ExperimentalKtorApi
import org.koin.ktor.ext.inject

@OptIn(ExperimentalKtorApi::class)
fun Route.usersRoute() {
    val userController: UserController by inject()

    postOrRaise("/auth/tokens") {
        val credentials = call.receiveOrRaise<LoginUserRequest>("Invalid credentials").bind()
        userController.login(credentials)
    }

    postOrRaise("/users") {
        val user = call.receiveOrRaise<RegisterUserRequest>("Invalid user").bind()
        userController.createUser(user.username, user.email, user.password)
    }.describe {
        summary = "Register user"
    }

    authenticate("auth-jwt") {
        route("/users") {
            route("/me") {
                deleteOrRaise {
                    val username = getUsername()
                    userController.removeUser(username)
                }.describe {
                    summary = "Remove user"
                }

                route("/wallets") {
                    getOrRaise {
                        val username = getUsername()
                        userController.getWallets(username)
                    }.describe {
                        summary = "Get user wallets"
                    }

                    postOrRaise {
                        val username = getUsername()
                        val wallets = call.receiveOrRaise<Wallets>("Invalid wallets").bind()
                        userController.addWallets(username, wallets.content)
                    }.describe {
                        summary = "Add wallets for user"
                    }

                    deleteOrRaise {
                        val username = getUsername()
                        val wallets = call.receiveOrRaise<Wallets>("Invalid wallets").bind()
                        userController.removeWallets(username, wallets.content)
                    }.describe {
                        summary = "Remove wallets for user"
                    }
                }
            }
        }
    }
}

fun RoutingContext.getUsername(): String {
    val principal = call.principal<JWTPrincipal>()
    return principal!!.payload.getClaim("username").asString()
}

fun RoutingContext.getRole(): UserRole? {
    val principal = call.principal<JWTPrincipal>()
    return principal?.payload?.getClaim("role")?.asString()?.let {
        UserRole.valueOf(it)
    }
}