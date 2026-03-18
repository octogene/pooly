package dev.octogene.pooly.common.mobile

import kotlinx.coroutines.flow.Flow

interface CredentialRepository {
    val isLogged: Flow<Boolean>
    suspend fun setLogin(username: String, password: String)
    fun getLogin(): Pair<String, String>?
    suspend fun setToken(token: String, refreshToken: String? = null)
    fun getToken(): Pair<String, String?>?
}
