package dev.octogene.pooly.login

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dev.octogene.pooly.common.mobile.CredentialRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "credentials")

@ContributesTo(AppScope::class)
@BindingContainer
class LoginContainer {
    @Provides
    fun provideCredentialDataStore(context: Context): DataStore<Preferences> = context.dataStore

    @Provides
    fun provideCredentialRepository(dataStore: DataStore<Preferences>): CredentialRepository =
        CredentialRepositoryImpl(dataStore)
}
