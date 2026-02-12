package dev.octogene.pooly.common.cache.di

import dev.octogene.pooly.common.cache.CacheClient
import dev.octogene.pooly.common.cache.CacheType
import dev.octogene.pooly.common.cache.InMemoryCacheClient
import dev.octogene.pooly.common.cache.ValkeyCacheClient
import dev.octogene.pooly.common.cache.config.CacheConfig
import io.lettuce.core.RedisClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose

val cacheModule = { config: CacheConfig ->
    module {
        single {
            RedisClient.create("redis://${config.host}:${config.port}")
        }.onClose {
            it?.close()
        }
        single<CacheClient>(named(CacheType.VALKEY)) {
            ValkeyCacheClient(get(), get())
        }
        single<CacheClient>(named(CacheType.INMEMORY)) {
            InMemoryCacheClient(cleanupInterval = config.cleanupInterval,)
        }
    }
}
