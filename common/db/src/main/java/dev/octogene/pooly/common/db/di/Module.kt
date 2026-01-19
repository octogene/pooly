package dev.octogene.pooly.common.db.di

import dev.octogene.pooly.common.db.repository.PrizeRepository
import dev.octogene.pooly.common.db.repository.PrizeRepositoryImpl
import dev.octogene.pooly.common.db.repository.UserRepository
import dev.octogene.pooly.common.db.repository.UserRepositoryImpl
import dev.octogene.pooly.common.db.repository.VaultRepository
import dev.octogene.pooly.common.db.repository.VaultRepositoryImpl
import dev.octogene.pooly.common.db.repository.WalletRepository
import dev.octogene.pooly.common.db.repository.WalletRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoriesModule = module {
    singleOf(::PrizeRepositoryImpl) bind PrizeRepository::class
    single<UserRepository> {
        UserRepositoryImpl(get(), get(named("password-hasher")))
    }
    singleOf(::VaultRepositoryImpl) bind VaultRepository::class
    singleOf(::WalletRepositoryImpl) bind WalletRepository::class
}
