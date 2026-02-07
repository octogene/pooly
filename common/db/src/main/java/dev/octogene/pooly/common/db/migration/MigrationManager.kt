package dev.octogene.pooly.common.db.migration

import org.flywaydb.core.Flyway
import javax.sql.DataSource

class MigrationManager(
    private val dataSource: DataSource,
    migrationsLocation: String = "classpath:$MIGRATIONS_DIRECTORY",
    baselineOnMigrate: Boolean = false,
) {
    private val flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations(migrationsLocation)
        .loggers("slf4j")
        .baselineOnMigrate(baselineOnMigrate)
        .load()

    fun migrate() {
        flyway.migrate()
    }
}
