package dev.octogene.pooly.server.di

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import dev.octogene.pooly.server.config.DbConfig
import dev.octogene.pooly.server.config.SecurityConfig
import dev.octogene.pooly.server.prize.PrizeRepository
import dev.octogene.pooly.server.prize.PrizeRepositoryImpl
import dev.octogene.pooly.server.security.JwtGenerator
import dev.octogene.pooly.server.security.PasswordHasher
import dev.octogene.pooly.server.user.UserController
import dev.octogene.pooly.server.user.UserRepository
import dev.octogene.pooly.server.user.UserRepositoryImpl
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.dsl.module

val persistenceModule = { config: DbConfig ->
    module {
        single {
            if (config.driver == "org.h2.Driver") {
                Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", config.driver)
            } else {
                Database.connect(
                    "jdbc:postgresql://${config.host}:${config.port}/${config.name}",
                    config.driver,
                    config.username,
                    config.password
                )
            }
        }
    }
}

val userModule = {
    module {
        single<UserRepository> {
            UserRepositoryImpl(get(), get())
        }
        single<UserController> {
            UserController(get())
        }
        single<PrizeRepository> {
            PrizeRepositoryImpl(get())
        }
    }
}

val securityModule = { config: SecurityConfig ->
    module {
        single {
            PasswordHasher(
                config.hashing.argon2Type,
                config.hashing.iterations,
                config.hashing.memory,
                config.hashing.parallelism
            )
        }

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
