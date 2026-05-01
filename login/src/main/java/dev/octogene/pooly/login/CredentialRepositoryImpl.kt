package dev.octogene.pooly.login

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import dev.octogene.pooly.common.mobile.CredentialRepository
import dev.octogene.pooly.common.mobile.security.CryptoManager
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlin.io.encoding.Base64

@Inject
class CredentialRepositoryImpl(private val dataStore: DataStore<Preferences>) : CredentialRepository {

    override val isLogged: Flow<Boolean>
        get() = dataStore.data.map { it[KEY_TOKEN] != null }

    companion object {
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_PASSWORD = stringPreferencesKey("password")
        private val KEY_TOKEN = stringPreferencesKey("auth_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    override suspend fun setLogin(username: String, password: String) {
        val encryptedPassword = Base64.encode(
            CryptoManager.encrypt(password),
        )
        dataStore.edit { preferences ->
            preferences[KEY_USERNAME] = username
            preferences[KEY_PASSWORD] = encryptedPassword
        }
    }

    override fun getLogin(): Pair<String, String>? = runBlocking {
        val prefs = dataStore.data.first()
        val user = prefs[KEY_USERNAME] ?: return@runBlocking null
        val encryptedPassword = prefs[KEY_PASSWORD] ?: return@runBlocking null

        runCatching {
            val decryptedPassword = CryptoManager.decrypt(Base64.decode(encryptedPassword))
            user to decryptedPassword
        }.onFailure {
            Logger.e { "Failed to decrypt password: ${it.message}" }
        }.getOrNull()
    }

    override fun getToken(): Pair<String, String?>? = runBlocking {
        val preferences = dataStore.data.first()
        val authToken = preferences[KEY_TOKEN]
        val refreshToken = preferences[REFRESH_TOKEN]
        authToken?.let { authToken to refreshToken }
    }

    override suspend fun setToken(token: String, refreshToken: String?) {
        dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
            refreshToken?.let {
                preferences[REFRESH_TOKEN] = refreshToken
            }
        }
    }
}
