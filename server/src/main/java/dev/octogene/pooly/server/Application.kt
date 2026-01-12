package dev.octogene.pooly.server

import arrow.raise.ktor.server.routing.getOrRaise
import com.sksamuel.cohort.Cohort
import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.cpu.ProcessCpuHealthCheck
import com.sksamuel.cohort.memory.FreememHealthCheck
import dev.octogene.pooly.server.config.AppConfig
import dev.octogene.pooly.server.config.Metrics
import dev.octogene.pooly.server.di.persistenceModule
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.getAs
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.ExperimentalSerializationApi
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

    install(Koin) {
        slf4jLogger()

        // Declare modules
        modules(persistenceModule(config.database))
    }

    routing()
    metrics(config.metrics)

    if (developmentMode) {
        install(CallLogging)
    }

    install(ContentNegotiation) {
        protobuf()
        json()
    }
}

fun Application.routing() {
    routing {
        route("/api/v1") {
            prizesRoute()
            walletsRoute()
        }
    }
}

fun Route.walletsRoute() {
    getOrRaise("/wallets") {
        "Hello, world!"
    }
}

fun Route.prizesRoute() {
    getOrRaise("/prizes/latest") {
        "Hello, world!"
    }
    getOrRaise("/prizes/{wallet}/latest") {
        "Hello, world!"
    }
}

private fun Application.metrics(metrics: Metrics) {
    val healthChecks = HealthCheckRegistry(Dispatchers.Default) {
        register(FreememHealthCheck.mb(metrics.minFreeMem), 30.seconds, 1.minutes)
        register(ProcessCpuHealthCheck(metrics.maxLoad), 30.seconds, 1.minutes)
    }
    install(Cohort) { healthcheck("/health", healthChecks) }
}
