package dev.octogene.pooly.server

import com.auth0.jwt.interfaces.JWTVerifier
import com.sksamuel.cohort.Cohort
import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.cpu.ProcessCpuHealthCheck
import com.sksamuel.cohort.memory.FreememHealthCheck
import dev.octogene.pooly.common.db.checkDatabaseInitialization
import dev.octogene.pooly.common.db.di.repositoriesModule
import dev.octogene.pooly.server.config.AppConfig
import dev.octogene.pooly.server.config.Metrics
import dev.octogene.pooly.server.di.controllerModule
import dev.octogene.pooly.server.di.persistenceModule
import dev.octogene.pooly.server.di.securityModule
import dev.octogene.pooly.server.prize.prizesRoute
import dev.octogene.pooly.server.user.usersRoute
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.getAs
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>) {
    val config = ApplicationConfig("application.yaml")
        .property("app")
        .getAs<AppConfig>()

    embeddedServer(Netty, host = config.host, port = config.port) {
        app(config)
    }.start(wait = true)
}

@OptIn(ExperimentalSerializationApi::class)
fun Application.app(config: AppConfig) {
    dependencies(config)

    val jwtVerifier: JWTVerifier by inject<JWTVerifier>()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Access to pooly application"
            verifier(jwtVerifier)
            validate { credential ->
                credential.payload.getClaim("username").asString()?.let {
                    JWTPrincipal(credential.payload)
                }
            }
        }
    }

    routing()
    metrics(config.metrics)

    if (developmentMode) {
        install(CallLogging)
    }

    install(ContentNegotiation) {
        json()
        protobuf()
    }
}

fun Application.dependencies(config: AppConfig) {
    install(Koin) {
        slf4jLogger()
        modules(persistenceModule(config.database), repositoriesModule, controllerModule(), securityModule(config.security))
    }
    launch {
        checkDatabaseInitialization(get())
    }
}

fun Application.routing() {
    routing {
        route("/api/v1") {
            usersRoute()
            prizesRoute()
        }
    }
}

fun Application.metrics(metrics: Metrics) {
    val healthChecks = HealthCheckRegistry(Dispatchers.Default) {
        register(FreememHealthCheck.mb(metrics.minFreeMem), 30.seconds, 1.minutes)
        register(ProcessCpuHealthCheck(metrics.maxLoad), 30.seconds, 1.minutes)
    }
    install(Cohort) { healthcheck("/health", healthChecks) }
}
