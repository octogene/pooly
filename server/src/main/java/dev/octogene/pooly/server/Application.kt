package dev.octogene.pooly.server

import com.auth0.jwt.interfaces.JWTVerifier
import com.sksamuel.cohort.Cohort
import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.cpu.ProcessCpuHealthCheck
import com.sksamuel.cohort.logback.LogbackManager
import com.sksamuel.cohort.memory.FreememHealthCheck
import dev.octogene.pooly.common.cache.CacheClient
import dev.octogene.pooly.common.cache.di.cacheModule
import dev.octogene.pooly.common.db.di.repositoriesModule
import dev.octogene.pooly.common.db.migration.MigrationManager
import dev.octogene.pooly.server.admin.adminRoutes
import dev.octogene.pooly.server.config.AppConfig
import dev.octogene.pooly.server.config.Metrics
import dev.octogene.pooly.server.di.controllerModule
import dev.octogene.pooly.server.di.persistenceModule
import dev.octogene.pooly.server.di.securityModule
import dev.octogene.pooly.server.model.ApiKeyPrincipal
import dev.octogene.pooly.server.prize.prizesRoute
import dev.octogene.pooly.server.user.usersRoute
import io.ktor.openapi.OpenApiInfo
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.apikey.apiKey
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.getAs
import io.ktor.server.engine.EngineConnectorBuilder
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.HttpRequestLifecycle
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.qualifier.named
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import java.net.ConnectException
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>) {
    val config = ApplicationConfig("application.yaml")
        .property("app")
        .getAs<AppConfig>()

    embeddedServer(Netty, configure = {
        connectors.add(
            EngineConnectorBuilder().apply {
                host = config.host
                port = config.port
            },
        )
        enableHttp2 = true
        tcpKeepAlive = true
    }) {
        app(config)
    }.start(wait = true)
}

@OptIn(ExperimentalSerializationApi::class)
fun Application.app(config: AppConfig) {
    install(CallLogging) {
        logger = LoggerFactory.getLogger("CallLogging")
    }

    install(HttpRequestLifecycle) {
        cancelCallOnClose = true
    }

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
        jwt("auth-admin-jwt") {
            realm = "Access to pooly application"
            verifier(jwtVerifier)
            validate { credential ->
                if (credential.payload.getClaim("role").asString().lowercase() == "admin") {
                    credential.payload.getClaim("username").asString()?.let {
                        JWTPrincipal(credential.payload)
                    }
                }
            }
        }
        apiKey("auth-admin") {
            validate { keyFromHeader ->
                // TODO: Use a DB
                val expectedApiKey = config.security.apikey
                keyFromHeader
                    .takeIf { it == expectedApiKey }
                    ?.let { ApiKeyPrincipal(keyFromHeader) }
            }
        }
    }

    routing()
    metrics(config.metrics)

    install(ContentNegotiation) {
        json()
        protobuf()
    }

    initialization(config)
}

fun Application.dependencies(config: AppConfig) {
    install(Koin) {
        slf4jLogger()
        modules(
            persistenceModule(config.database),
            cacheModule(config.cache),
            repositoriesModule,
            controllerModule(config.cache.type),
            securityModule(config.security),
        )
    }
}

fun Application.initialization(config: AppConfig) {
    launch {
        try {
            val migrationManager: MigrationManager by inject()
            migrationManager.migrate()
        } catch (error: ConnectException) {
            log.error("Failed to connect to the database : {}", error.message)
            exitProcess(1)
        }
    }

    launch {
        val cacheClient: CacheClient by inject(named(config.cache.type))
        cacheClient.initialize()
    }
}

fun Application.routing() {
    routing {
        route("/api/v1") {
            openAPI(path = "openapi") {
                info = OpenApiInfo(
                    title = "Pooly API",
                    version = "1.0.0",
                    description = "A PoolTogether prize checker API for the Pooly app",
                )
            }
            usersRoute()
            prizesRoute()
            adminRoutes()
        }
    }
}

fun Application.metrics(metrics: Metrics, dispatcher: CoroutineDispatcher = Dispatchers.Default) {
    val healthChecks = HealthCheckRegistry(dispatcher) {
        register(FreememHealthCheck.mb(metrics.minFreeMem), 30.seconds, 1.minutes)
        register(ProcessCpuHealthCheck(metrics.maxLoad), 30.seconds, 1.minutes)
    }
    install(Cohort) {
        logManager = LogbackManager
        healthcheck("/health", healthChecks)
    }
}
