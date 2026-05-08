@file:OptIn(ExperimentalDatabaseMigrationApi::class)

package dev.octogene.pooly.common.db.migration

import dev.octogene.pooly.common.db.table.Prizes
import dev.octogene.pooly.common.db.table.Users
import dev.octogene.pooly.common.db.table.Vaults
import dev.octogene.pooly.common.db.table.Wallets
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.time.Clock

fun main() {
    val dbUrl = System.getProperty("db.url")
    val dbUser = System.getProperty("db.user")
    val dbPassword = System.getProperty("db.password")
    val migrationName = System.getProperty("migration.name")
    val tables = System.getProperty("migration.tables").split(",")
    val postgresql = Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPassword,
    )

    transaction(postgresql) {
        generateMigrationScript(migrationName, tables)
    }
}

const val MIGRATIONS_DIRECTORY = "src/main/resources/migrations"

fun generateMigrationScript(name: String? = null, tableNames: List<String>) {
    val tables = tableNames.map { tableName ->
        when (tableName.lowercase()) {
            "users" -> Users
            "prizes" -> Prizes
            "vaults" -> Vaults
            "wallets" -> Wallets
            else -> throw IllegalArgumentException("Unknown table name $tableName")
        }
    }.toTypedArray()

    val timestamp = Clock.System.now().format(
        DateTimeComponents.Format {
            year()
            monthNumber()
            day()
        },
    )

    val migrationFiles =
        Path(MIGRATIONS_DIRECTORY).listDirectoryEntries("V$timestamp*")
    val nextSequence = migrationFiles.size + 1

    MigrationUtils.generateMigrationScript(
        scriptDirectory = MIGRATIONS_DIRECTORY,
        scriptName = "V$timestamp.${nextSequence}__$name",
        tables = tables,
    )
}
