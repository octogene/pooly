package dev.octogene.pooly.common.db.migration

import org.flywaydb.core.Flyway

class MigrationManager(
    databaseUrl: String,
    databaseUser: String,
    databasePassword: String,
    migrationsLocation: String = "classpath:$MIGRATIONS_DIRECTORY",
    baselineOnMigrate: Boolean = false
) {


    private val flyway = Flyway.configure()
        .dataSource(databaseUrl, databaseUser, databasePassword)
        .locations(migrationsLocation)
        .loggers("slf4j")
        .baselineOnMigrate(baselineOnMigrate)
        .load()


    fun migrate() {
        val info = flyway.info()
        println(info.infoResult.migrations.map { it.filepath })
        flyway.migrate()
    }
}
