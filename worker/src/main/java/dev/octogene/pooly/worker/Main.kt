package dev.octogene.pooly.worker

import arrow.continuations.SuspendApp
import arrow.resilience.Schedule
import arrow.resilience.retry
import ch.qos.logback.classic.Logger
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import dev.octogene.pooly.common.db.checkDatabaseInitialization
import dev.octogene.pooly.common.db.di.repositoriesModule
import dev.octogene.pooly.worker.di.workerModule
import dev.octogene.pooly.worker.model.AppConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

fun main() = SuspendApp {
    val logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
    logger.info("Starting process")
    val config = ConfigLoaderBuilder.default()
        .addResourceSource("/application.yaml")
        .build()
        .loadConfigOrThrow<AppConfig>(prefix = "app")
    logger.info("Configuration loaded successfully")

    startKoin {
        modules(
            repositoriesModule,
            workerModule(config)
        )
    }

    val koin = getKoin()
    val retrySchedule = Schedule.recurs<Throwable>(3)
        .and(Schedule.spaced(20.seconds))
    val database = koin.get<Database>()
    try {
        retrySchedule.retry<Throwable, Unit> {
            checkDatabaseInitialization(database)
        }
        logger.info("Database initialized successfully.")
        val worker = koin.get<Worker>()
        worker.run()
    } catch (e: Exception) {
        logger.error("Failed to initialize database after multiple retries: ${e.message}", e)
        throw e
    }
}
