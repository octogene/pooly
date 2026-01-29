package dev.octogene.pooly.server.di

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import dev.octogene.pooly.server.cache.CacheClient
import dev.octogene.pooly.server.cache.CacheType
import dev.octogene.pooly.server.cache.InMemoryCacheClient
import dev.octogene.pooly.server.cache.ValkeyCacheClient
import dev.octogene.pooly.server.config.CacheConfig
import dev.octogene.pooly.server.config.DbConfig
import dev.octogene.pooly.server.config.SecurityConfig
import dev.octogene.pooly.server.prize.PrizeController
import dev.octogene.pooly.server.security.JwtGenerator
import dev.octogene.pooly.server.security.PasswordHasher
import dev.octogene.pooly.server.security.Argon2PasswordHasher
import dev.octogene.pooly.server.user.UserController
import eu.vendeli.rethis.ReThis
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val persistenceModule = { dbConfig: DbConfig, cacheConfig: CacheConfig ->
    module {
        single {
            if (dbConfig.driver == "org.h2.Driver") {
                Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MYSQL;", dbConfig.driver)
            } else {
                Database.connect(
                    "jdbc:postgresql://${dbConfig.host}:${dbConfig.port}/${dbConfig.name}",
                    dbConfig.driver,
                    dbConfig.username,
                    dbConfig.password
                )
            }
        }
        single {
            ReThis(
                host = cacheConfig.host,
                port = cacheConfig.port
            )
        }
        single<CacheClient>(named(CacheType.VALKEY)) {
            ValkeyCacheClient(get(), cacheConfig.defaultTTL)
        }
        single<CacheClient>(named(CacheType.INMEMORY)) {
            InMemoryCacheClient(
                defaultTTL = cacheConfig.defaultTTL,
                cleanupInterval = cacheConfig.cleanupInterval
            )
        }
    }
}

val controllerModule = { cacheType: CacheType ->
    module {
        singleOf(::UserController)
        single {
            PrizeController(
                get(named(cacheType)),
                get(),
                get(),
                get()
            )
        }
    }
}

val securityModule = { config: SecurityConfig ->
    module {
        single<PasswordHasher> {
            Argon2PasswordHasher(
                config.hashing.argon2Type,
                config.hashing.iterations,
                config.hashing.memory,
                config.hashing.parallelism
            )
        }

        // TODO: Properly share
        single<(String) -> String>(named("password-hasher")) {
            { password: String ->
                val passwordHasher = get<PasswordHasher>()
                passwordHasher.hash(password)
            }
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
