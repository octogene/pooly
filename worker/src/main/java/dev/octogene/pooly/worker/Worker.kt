package dev.octogene.pooly.worker

import dev.octogene.pooly.ptgraph.api.PoolTogetherGraphQLClient
import dev.octogene.pooly.rpc.PoolTogetherRPCClient
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.exposed.v1.jdbc.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Worker(
    private val rpcClient: PoolTogetherRPCClient,
    private val graphClient: PoolTogetherGraphQLClient,
    private val database: Database,
    private val logger: Logger = LoggerFactory.getLogger(Worker::class.java)
) {
    suspend fun run() = coroutineScope {
        logger.info("Starting worker")
        while (isActive) {
            // fetchData()
            // updateData()
            delay(1000)
        }
    }
}
