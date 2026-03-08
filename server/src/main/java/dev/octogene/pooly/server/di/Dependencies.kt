package dev.octogene.pooly.server.di

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.octogene.pooly.common.backend.security.PasswordHasher
import dev.octogene.pooly.common.backend.security.PasswordVerifier
import dev.octogene.pooly.common.cache.CacheType
import dev.octogene.pooly.common.db.migration.MigrationManager
import dev.octogene.pooly.server.admin.AdminController
import dev.octogene.pooly.server.config.DbConfig
import dev.octogene.pooly.server.config.SecurityConfig
import dev.octogene.pooly.server.prize.PrizeController
import dev.octogene.pooly.server.security.Argon2PasswordHasher
import dev.octogene.pooly.server.security.AuthenticationService
import dev.octogene.pooly.server.security.JwtManager
import dev.octogene.pooly.server.user.UserController
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module
import org.koin.dsl.onClose
import javax.sql.DataSource

val persistenceModule = { dbConfig: DbConfig ->
    module {
        single<HikariDataSource> {
            val config = HikariConfig().apply {
                if (dbConfig.driver == "org.h2.Driver") {
                    jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MYSQL;"
                    driverClassName = dbConfig.driver
                } else {
                    jdbcUrl = "jdbc:postgresql://${dbConfig.host}:${dbConfig.port}/${dbConfig.name}"
                    driverClassName = dbConfig.driver
                    username = dbConfig.username
                    password = dbConfig.password
                    maximumPoolSize = 6
                    isReadOnly = false
                    transactionIsolation = "TRANSACTION_SERIALIZABLE"
                }
            }
            HikariDataSource(config)
        }.onClose {
            it?.close()
        } bind DataSource::class

        single { Database.connect(datasource = get<HikariDataSource>()) }

        single {
            MigrationManager(
                dataSource = get(),
                migrationsLocation = "migrations/",
                baselineOnMigrate = false,
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
                userRepository = get(),
            )
        }
        single {
            AdminController(
                cacheClient = get(named(cacheType)),
                authenticationService = get(),
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
                jwtManager = get(),
            )
        }

        single {
            Argon2PasswordHasher(
                config.hashing.argon2Type,
                config.hashing.iterations,
                config.hashing.memory,
                config.hashing.parallelism,
            )
        } binds arrayOf(PasswordHasher::class, PasswordVerifier::class)

        single {
            val algorithm = Algorithm.HMAC256(config.jwt.secret)
            JwtManager(
                creator = JWT.create()
                    .withAudience(config.jwt.audience)
                    .withIssuer(config.jwt.issuer),
                algorithm = algorithm,
                verifier = JWT
                    .require(algorithm)
                    .withAudience(config.jwt.audience)
                    .withIssuer(config.jwt.issuer)
                    .build(),
            )
        } bind JWTVerifier::class
    }
}
