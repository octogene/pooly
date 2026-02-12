package dev.octogene.pooly.worker.di

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.octogene.pooly.common.db.migration.MigrationManager
import dev.octogene.pooly.ptgraph.api.PoolTogetherGraphQLClient
import dev.octogene.pooly.rpc.PoolTogetherRPCClient
import dev.octogene.pooly.worker.Worker
import dev.octogene.pooly.worker.model.AppConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import javax.sql.DataSource

val workerModule = { config: AppConfig ->
    module {
        single<HikariDataSource> {
            val config = HikariConfig().apply {
                if (config.database.driver == "org.h2.Driver") {
                    jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MYSQL;"
                    driverClassName = config.database.driver
                } else {
                    jdbcUrl =
                        "jdbc:postgresql://${config.database.host}:${config.database.port}/${config.database.name}"
                    driverClassName = config.database.driver
                    username = config.database.username
                    password = config.database.password
                    maximumPoolSize = 6
                    isReadOnly = false
                    transactionIsolation = "TRANSACTION_SERIALIZABLE"
                }
            }
            HikariDataSource(config)
        } bind DataSource::class

        single { Database.connect(datasource = get<HikariDataSource>()) }

        single { PoolTogetherRPCClient() }
        single { PoolTogetherGraphQLClient() }
        single {
            Worker(
                rpcClient = get(),
                graphClient = get(),
                checkInterval = config.checkInterval,
                vaultRepository = get(),
                prizeRepository = get(),
                walletRepository = get(),
            )
        }
        single {
            MigrationManager(
                dataSource = get(),
                migrationsLocation = "migrations/",
                baselineOnMigrate = false,
            )
        }
    }
}
