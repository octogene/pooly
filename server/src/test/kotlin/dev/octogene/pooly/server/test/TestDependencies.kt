package dev.octogene.pooly.server.test

import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.server.prize.PrizeController
import dev.octogene.pooly.server.prize.PrizeRepository
import dev.octogene.pooly.server.user.User
import dev.octogene.pooly.server.user.UserController
import dev.octogene.pooly.server.user.UserRepository
import org.koin.dsl.module

val testUserModule = { users: List<User>, prizes: List<Prize> ->
    module {
        single<UserRepository> {
            FakeUserRepository(users.associateBy { it.username }.toMutableMap())
        }
        single<UserController> {
            UserController(get())
        }
        single<PrizeRepository> {
            FakePrizeRepository(prizes)
        }
        single<PrizeController> {
            PrizeController(get(), get())
        }
    }
}