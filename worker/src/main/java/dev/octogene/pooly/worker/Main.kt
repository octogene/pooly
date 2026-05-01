package dev.octogene.pooly.worker

import arrow.continuations.SuspendApp
import arrow.resilience.Schedule
import arrow.resilience.retry
import ch.qos.logback.classic.Logger
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.addResourceSource
import dev.octogene.pooly.common.db.di.repositoriesModule
import dev.octogene.pooly.common.db.migration.MigrationManager
import dev.octogene.pooly.worker.di.workerModule
import dev.octogene.pooly.worker.model.AppConfig
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

private const val MAX_RETRIES = 3L

@OptIn(ExperimentalHoplite::class)
fun main() = SuspendApp {
    val logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
    logger.info("Starting process")
    val config = ConfigLoaderBuilder.default()
        .withExplicitSealedTypes()
        .addResourceSource("/application.yaml")
        .build()
        .loadConfigOrThrow<AppConfig>(prefix = "app")
    logger.info("Configuration loaded successfully")

    startKoin {
        modules(
            repositoriesModule,
            workerModule(config),
        )
    }

    val koin = getKoin()
    initializeDatabase(koin, logger)
    val worker = koin.get<Worker>()
    worker.run()
}

private suspend fun initializeDatabase(koin: Koin, logger: org.slf4j.Logger) {
    val retrySchedule = Schedule.recurs<Throwable>(MAX_RETRIES)
        .and(Schedule.spaced(20.seconds))
    retrySchedule.retry<Throwable, Unit> {
        koin.get<MigrationManager>().migrate()
    }
    logger.info("Database initialized successfully.")
}
