package dev.octogene.pooly.server.test

import dev.octogene.pooly.common.db.repository.PrizeRepository
import dev.octogene.pooly.common.db.repository.UserRepository
import dev.octogene.pooly.common.db.repository.WalletRepository
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.server.prize.PrizeController
import dev.octogene.pooly.server.user.User
import dev.octogene.pooly.server.user.UserController
import org.koin.dsl.module

val testUserModule = { users: List<User>, prizes: List<Prize> ->
    module {
        single<UserRepository> {
            FakeUserRepository(users.associate {
                it.username to dev.octogene.pooly.core.User(
                    it.username,
                    it.email
                )
            }.toMutableMap())
        }
        single<UserController> {
            UserController(get(), get(), get())
        }
        single<PrizeRepository> {
            FakePrizeRepository(prizes)
        }
        single<PrizeController> {
            PrizeController(get(), get(), get())
        }
        single<WalletRepository> {
            FakeWalletRepository()
        }
    }
}