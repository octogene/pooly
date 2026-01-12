package dev.octogene.pooly.server.di

import dev.octogene.pooly.server.config.DbConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.dsl.module

val persistenceModule = { config: DbConfig ->
    module {
        single { Database.connect(config.url, config.driver) }
    }
}