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
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.postgresql.util.PSQLException
import org.slf4j.Logger

internal suspend fun <T> suspendTransactionOrRaise(
    database: Database,
    readOnly: Boolean = false,
    block: suspend context(Raise<RepositoryError>) JdbcTransaction.() -> T,
): Either<RepositoryError, T> = either {
    catch(
        block = {
            suspendTransaction(database, readOnly = readOnly) {
                block(this@suspendTransaction)
            }
        },
        catch = { throwable: Throwable ->
            when (throwable) {
                is ExposedSQLException -> {
                    raise(handleSQLException(throwable))
                }

                else -> raise(DatabaseError(throwable.message ?: "Unknown error"))
            }
        }
    )
}

fun handleSQLException(throwable: ExposedSQLException) = when (throwable.cause) {
    is JdbcSQLIntegrityConstraintViolationException -> {
        RepositoryError.AlreadyExists(throwable.message ?: "Unkown error")
    }

    is PSQLException -> {
        if (throwable.sqlState == "23505") {
            RepositoryError.AlreadyExists(throwable.message ?: "Unkown error")
        } else {
            DatabaseError(throwable.message ?: "Unknown error")
        }
    }

    else -> DatabaseError(throwable.message ?: "Unknown SQL error")
}

suspend fun checkDatabaseInitialization(database: Database, logger: Logger) {
    suspendTransaction(database) {
        SchemaUtils.create(Users, Wallets, Vaults, Prizes)
    }
}
