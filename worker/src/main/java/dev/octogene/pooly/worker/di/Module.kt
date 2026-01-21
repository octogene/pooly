package dev.octogene.pooly.worker.di

import dev.octogene.pooly.ptgraph.api.PoolTogetherGraphQLClient
import dev.octogene.pooly.rpc.PoolTogetherRPCClient
import dev.octogene.pooly.worker.Worker
import dev.octogene.pooly.worker.model.AppConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.qualifier.named
import org.koin.dsl.module

val workerModule = { config: AppConfig ->
    module {
        single {
            Database.connect(
                config.db.url,
                config.db.driver,
                config.db.user,
                config.db.password
            )
        }
        single { PoolTogetherRPCClient() }
        single { PoolTogetherGraphQLClient() }
        single {
            Worker(
                rpcClient = get(),
                graphClient = get(),
                database = get(),
                checkInterval = config.checkInterval,
                vaultRepository = get(),
                prizeRepository = get(),
                walletRepository = get(),
            )
        }
        // Dummy
        single<(String) -> String>(named("password-hasher")) {
            { rawPassword ->
                rawPassword
            }
        }
    }
}

