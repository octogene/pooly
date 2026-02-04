package dev.octogene.pooly.server.di

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import dev.octogene.pooly.common.backend.security.PasswordHasher
import dev.octogene.pooly.common.backend.security.PasswordVerifier
import dev.octogene.pooly.common.cache.CacheType
import dev.octogene.pooly.common.db.migration.MigrationManager
import dev.octogene.pooly.server.admin.AdminController
import dev.octogene.pooly.server.admin.ClearCacheRequest
import dev.octogene.pooly.server.config.DbConfig
import dev.octogene.pooly.server.config.SecurityConfig
import dev.octogene.pooly.server.prize.PrizeController
import dev.octogene.pooly.server.security.Argon2PasswordHasher
import dev.octogene.pooly.server.security.AuthenticationService
import dev.octogene.pooly.server.security.JwtGenerator
import dev.octogene.pooly.server.user.UserController
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.binds
import org.koin.dsl.module

val persistenceModule = { dbConfig: DbConfig ->
    module {
        single {
            if (dbConfig.driver == "org.h2.Driver") {
                Database.connect(
                    url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MYSQL;",
                    driver = dbConfig.driver
                )
            } else {
                Database.connect(
                    url = "jdbc:postgresql://${dbConfig.host}:${dbConfig.port}/${dbConfig.name}",
                    driver = dbConfig.driver,
                    user = dbConfig.username,
                    password = dbConfig.password
                )
            }
        }
        single {
            MigrationManager(
                databaseUrl = "jdbc:postgresql://${dbConfig.host}:${dbConfig.port}/${dbConfig.name}",
                databaseUser = dbConfig.username,
                databasePassword = dbConfig.password,
                migrationsLocation = "migrations/",
                baselineOnMigrate = false
            )
        }

        single<Json> { Json }
    }
}

val controllerModule = { cacheType: CacheType ->
    module {
        singleOf(::UserController)
        single {
            PrizeController(
                cacheClient = get(named(cacheType)),
                prizeRepository = get(),
                walletRepository = get(),
                userRepository = get()
            )
        }
        single {
            AdminController(
                cacheClient = get(named(cacheType))
            )
        }
    }
}

val securityModule = { config: SecurityConfig ->
    module {
        single {
            AuthenticationService(
                userRepository = get(),
                passwordHasher = get(),
                passwordVerifier = get(),
                jwtGenerator = get()
            )
        }

        single {
            Argon2PasswordHasher(
                config.hashing.argon2Type,
                config.hashing.iterations,
                config.hashing.memory,
                config.hashing.parallelism
            )
        } binds arrayOf(PasswordHasher::class, PasswordVerifier::class)

        single {
            JwtGenerator(config.jwt.secret)
        }

        single<JWTVerifier> {
            JWT
                .require(Algorithm.HMAC256(config.jwt.secret))
                .withAudience("audience")
                .withIssuer("issuer")
                .build()
        }
    }
}
