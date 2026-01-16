package dev.octogene.pooly.pooltogether

import co.touchlab.kermit.Logger
import dev.octogene.pooly.common.core.BuildConfig.POOLY_PASSWORD
import dev.octogene.pooly.common.core.BuildConfig.POOLY_USER
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import dev.octogene.pooly.pooltogether.db.DrawQueries
import dev.octogene.pooly.pooltogether.model.LoginRequest
import dev.octogene.pooly.pooltogether.model.LoginResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Inject
class PoolyApiClientImpl(
    private val drawQueries: DrawQueries,
    httpClient: HttpClient,
    private val base_url: String = "http://10.0.2.2:8080/api/v1"
) : PoolyApiClient {

    private val poolyClient: HttpClient = httpClient.config {
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens("initialInvalidToken", null)
                }
                refreshTokens {
                    val loginResponse = httpClient.post("$base_url/login") {
                        contentType(ContentType.Application.Json)
                        // TODO: Get from the settings
                        accept(ContentType.Application.Json)
                        setBody(LoginRequest(POOLY_USER, POOLY_PASSWORD))
                    }.body<LoginResponse>()
                    BearerTokens(loginResponse.token, null)
                }
            }
        }
    }

    override suspend fun registerUser(username: String, password: String, email: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getAllDraws(addresses: List<String>, network: ChainNetwork): List<Prize> =
        withContext(Dispatchers.IO) {
            // TODO: Should be done separately upon changes in settings
            registerWallets(addresses)
            // TODO: Should be paged
            val response = poolyClient.get("$base_url/prizes") {
                accept(ContentType.Application.Json)
            }.body<List<Prize>>()
            Logger.d { "getAllDraws response: $response" }
            response
        }

    override suspend fun getVaultsInfo(addresses: List<String>): List<Vault> {
        TODO("Not yet implemented")
    }

    override suspend fun registerWallets(addresses: List<String>) {
        withContext(Dispatchers.IO) {
            val response = poolyClient.post("$base_url/wallets") {
                contentType(ContentType.Application.Json)
                setBody(addresses)
            }
            Logger.d { "registerWallets response: $response" }
        }
    }
}

interface PoolyApiClient {
    suspend fun registerUser(username: String, password: String, email: String)
    suspend fun registerWallets(addresses: List<String>)
    suspend fun getAllDraws(address: List<String>, network: ChainNetwork): List<Prize>
    suspend fun getVaultsInfo(addresses: List<String>): List<Vault>
}
