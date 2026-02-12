package dev.octogene.pooly.server.test

import dev.octogene.pooly.common.backend.security.PasswordHasher
import dev.octogene.pooly.common.cache.CacheType
import dev.octogene.pooly.common.db.repository.PrizeRepository
import dev.octogene.pooly.common.db.repository.UserRepository
import dev.octogene.pooly.common.db.repository.WalletRepository
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.UserRole
import dev.octogene.pooly.server.prize.PrizeController
import dev.octogene.pooly.server.user.RegisterUserRequest
import dev.octogene.pooly.server.user.UserController
import org.koin.core.qualifier.named
import org.koin.dsl.module

val testUserModule = { users: List<RegisterUserRequest>, prizes: List<Prize>, cacheType: CacheType ->
    module {
        single<UserRepository> {
            val passwordHasher = get<PasswordHasher>()
            val userByUsername = users.associate {
                it.username to dev.octogene.pooly.core.User(
                    it.username,
                    it.email,
                    passwordHasher.hash(it.password),
                    UserRole.USER,
                )
            }.toMutableMap()
            FakeUserRepository(userByUsername)
        }
        single<UserController> {
            UserController(get(), get(), get())
        }
        single<PrizeRepository> {
            FakePrizeRepository(prizes)
        }
        single<PrizeController> {
            PrizeController(get(named(cacheType)), get(), get(), get())
        }
        single<WalletRepository> {
            FakeWalletRepository()
        }
    }
}
