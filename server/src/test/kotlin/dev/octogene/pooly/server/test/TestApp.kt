package dev.octogene.pooly.server.test

import com.auth0.jwt.interfaces.JWTVerifier
import dev.octogene.pooly.common.cache.CacheType
import dev.octogene.pooly.common.cache.config.CacheConfig
import dev.octogene.pooly.common.cache.di.cacheModule
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.server.config.AppConfig
import dev.octogene.pooly.server.config.DbConfig
import dev.octogene.pooly.server.config.HashingConfig
import dev.octogene.pooly.server.config.JwtConfig
import dev.octogene.pooly.server.config.Metrics
import dev.octogene.pooly.server.config.SecurityConfig
import dev.octogene.pooly.server.di.persistenceModule
import dev.octogene.pooly.server.di.securityModule
import dev.octogene.pooly.server.routing
import dev.octogene.pooly.server.user.RegisterUserRequest
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.apikey.apiKey
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSerializationApi::class)
fun Application.testApp(
    config: AppConfig,
    users: List<RegisterUserRequest> = emptyList(),
    prizes: List<Prize> = emptyList(),
) {
    install(Koin) {
        slf4jLogger()
        modules(
            persistenceModule(config.database),
            cacheModule(config.cache),
            testUserModule(users, prizes, config.cache.type),
            securityModule(config.security),
        )
    }

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
            validate { apiKeyFromHeader ->
                apiKeyFromHeader == "admin_api_key"
            }
        }
    }

    routing()

    install(ContentNegotiation) {
        json()
    }
}

val testAppConfig = AppConfig(
    host = "localhost",
    port = 8080,
    loglevel = "debug",
    metrics = Metrics(512, 0.75),
    database = DbConfig(
        host = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        port = 5437,
        name = "pooly_db",
        driver = "org.h2.Driver",
        username = "pooly",
        password = "pooly",
    ),
    security = SecurityConfig(
        jwt = JwtConfig("test", "octogene.dev", "pooly-api", 10.seconds),
        hashing = HashingConfig("Argon2id", 2, 20000, 1),
        apikey = "admin_api_key",
    ),
    cache = CacheConfig(
        type = CacheType.INMEMORY,
        host = "localhost",
        port = 5437,
        defaultTTL = 2.minutes,
        cleanupInterval = 1.minutes,
    ),
)
