package dev.octogene.pooly.worker

import arrow.continuations.SuspendApp
import arrow.resilience.Schedule
import arrow.resilience.retry
import ch.qos.logback.classic.Logger
import dev.octogene.pooly.common.db.checkDatabaseInitialization
import dev.octogene.pooly.common.db.di.repositoriesModule
import dev.octogene.pooly.ptgraph.api.PoolTogetherGraphQLClient
import dev.octogene.pooly.rpc.PoolTogetherRPCClient
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.getKoin
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

fun main() = SuspendApp {
    val logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
    logger.info("Starting process")
    val dbUrl = System.getenv("DB_URL") ?: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    val dbDriver = System.getenv("DB_DRIVER") ?: "org.h2.Driver"
    val dbUser = System.getenv("DB_USERNAME") ?: ""
    val dbPassword = System.getenv("DB_PASSWORD") ?: ""
    val checkInterval = System.getenv("CHECK_INTERVAL") ?: "5m"

    startKoin {
        modules(
            repositoriesModule,
            module {
                single { Database.connect(dbUrl, dbDriver, dbUser, dbPassword) }
                single { PoolTogetherRPCClient() }
                single { PoolTogetherGraphQLClient() }
                single {
                    Worker(
                        rpcClient = get(),
                        graphClient = get(),
                        database = get(),
                        checkInterval = checkInterval,
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
        )
    }
    val retrySchedule = Schedule.recurs<Throwable>(3)
        .and(Schedule.spaced(20.seconds))
    val database = getKoin().get<Database>()
    try {
        retrySchedule.retry<Throwable, Unit> {
            checkDatabaseInitialization(database)
        }
        logger.info("Database initialized successfully.")
        val worker = getKoin().get<Worker>()
        worker.run()
    } catch (e: Exception) {
        logger.error("Failed to initialize database after multiple retries: ${e.message}", e)
        throw e
    }
}
