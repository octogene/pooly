package dev.octogene.pooly.worker

import arrow.continuations.SuspendApp
import ch.qos.logback.classic.Logger
import dev.octogene.pooly.ptgraph.api.PoolTogetherGraphQLClient
import dev.octogene.pooly.rpc.PoolTogetherRPCClient
import org.jetbrains.exposed.v1.jdbc.Database
import org.slf4j.LoggerFactory

fun main() = SuspendApp {
    val logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
    logger.info("Starting process")
    val dbUrl = System.getenv("DB_URL") ?: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    val dbDriver = System.getenv("DB_DRIVER") ?: "org.h2.Driver"
    val dbUser = System.getenv("DB_USERNAME") ?: ""
    val dbPassword = System.getenv("DB_PASSWORD") ?: ""
    val checkInterval = System.getenv("CHECK_INTERVAL") ?: "5m"
    val worker = Worker(
        rpcClient = PoolTogetherRPCClient(),
        graphClient = PoolTogetherGraphQLClient(),
        database = Database.connect(dbUrl, dbDriver, dbUser, dbPassword),
        checkInterval = checkInterval
    )
    worker.run()
}
