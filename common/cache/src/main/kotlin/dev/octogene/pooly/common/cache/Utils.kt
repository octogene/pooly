package dev.octogene.pooly.common.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend fun checkCacheInitialization(cacheClient: CacheClient) = withContext(Dispatchers.IO) {
    when (this) {
        is InMemoryCacheClient -> {
            launch { runBackgroundCleanup() }
        }
        else -> {}
    }
}