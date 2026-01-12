package dev.octogene.pooly

import android.content.Context
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.octogene.pooly.db.Database
import dev.octogene.pooly.pooltogether.db.DrawQueries
import dev.octogene.pooly.pooltogether.db.Vault
import dev.octogene.pooly.pooltogether.db.VaultQueries
import dev.octogene.pooly.settings.db.NetworkState
import dev.octogene.pooly.settings.db.NetworkStateQueries
import dev.octogene.pooly.settings.db.WalletQueries
import dev.octogene.pooly.shared.model.ChainNetwork
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides

@BindingContainer
class DatabaseBindings {
    @Provides
    fun provideDatabase(sqlDriver: SqlDriver): Database = Database(
        sqlDriver,
        NetworkStateAdapter = NetworkState.Adapter(
            nameAdapter = EnumColumnAdapter<ChainNetwork>()
        ),
        VaultAdapter = Vault.Adapter(networkAdapter = EnumColumnAdapter<ChainNetwork>())
    )

    @Provides
    fun provideSqlDriver(@Includes context: Context): SqlDriver =
        AndroidSqliteDriver(Database.Schema, context, "pooly.db")

    @Provides
    fun provideNetworkStateQueries(database: Database): NetworkStateQueries =
        database.networkStateQueries

    @Provides
    fun provideVaultQueries(database: Database): VaultQueries =
        database.vaultQueries

    @Provides
    fun provideDrawQueries(database: Database): DrawQueries =
        database.drawQueries

    @Provides
    fun provideWalletQueries(database: Database): WalletQueries =
        database.walletQueries
}
