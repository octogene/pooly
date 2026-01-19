package dev.octogene.pooly.common.db

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.either
import dev.octogene.pooly.common.db.repository.RepositoryError
import dev.octogene.pooly.common.db.repository.RepositoryError.DatabaseError
import dev.octogene.pooly.common.db.table.Prizes
import dev.octogene.pooly.common.db.table.Users
import dev.octogene.pooly.common.db.table.Vaults
import dev.octogene.pooly.common.db.table.Wallets
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

internal suspend fun <T> suspendTransactionOrRaise(
    database: Database,
    readOnly: Boolean = false,
    block: suspend context(Raise<RepositoryError>) () -> T,
): Either<RepositoryError, T> = either {
    catch(
        block = {
            suspendTransaction(database, readOnly = readOnly) {
                block(this@either)
            }
        },
        catch = { throwable: Throwable ->
            raise(DatabaseError(throwable.message ?: "Unknown error"))
        }
    )
}

suspend fun checkDatabaseInitialization(database: Database) {
    suspendTransaction(database) {
        SchemaUtils.create(Users, Wallets, Vaults, Prizes)
    }
}
