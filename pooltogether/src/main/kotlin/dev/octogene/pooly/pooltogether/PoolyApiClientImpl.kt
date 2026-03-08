package dev.octogene.pooly.pooltogether

import arrow.core.Either
import arrow.core.raise.either
import co.touchlab.kermit.Logger
import dev.octogene.pooly.common.core.BuildConfig.POOLY_PASSWORD
import dev.octogene.pooly.common.core.BuildConfig.POOLY_USER
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import dev.octogene.pooly.pooltogether.db.DrawQueries
import dev.octogene.pooly.pooltogether.model.CallError
import dev.octogene.pooly.pooltogether.model.CreateUserRequest
import dev.octogene.pooly.pooltogether.model.LoginRequest
import dev.octogene.pooly.pooltogether.model.LoginResponse
import dev.octogene.pooly.pooltogether.model.getOrEither
import dev.octogene.pooly.pooltogether.model.postOrEither
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Inject
internal class PoolyApiClientImpl(
    private val drawQueries: DrawQueries,
    httpClient: HttpClient,
    private val baseUrl: String = BuildConfig.POOLY_BASE_URL,
) : PoolyApiClient {

    private val poolyClient: HttpClient = httpClient.config {
        install(Auth) {
            bearer {
                refreshTokens {
                    val loginResponse = httpClient.post("$baseUrl/auth/tokens") {
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

    override suspend fun registerUser(username: String, password: String, email: String): Either<CallError, Unit> =
        either {
            poolyClient.postOrEither<String>("$baseUrl/users") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(CreateUserRequest(username, password, email))
            }.bind()
        }

    override suspend fun getAllDraws(addresses: List<String>, network: ChainNetwork): Either<CallError, List<Prize>> =
        either {
            withContext(Dispatchers.IO) {
                // TODO: Should be paged
                val response = poolyClient.getOrEither<List<Prize>>("$baseUrl/users/me/prizes") {
                    accept(ContentType.Application.Json)
                }.bind()
                Logger.d { "getAllDraws response: $response" }
                response
            }
        }

    override suspend fun getVaultsInfo(addresses: List<String>): Either<CallError, List<Vault>> = either {
        TODO("Not yet implemented")
    }

    override suspend fun registerWallets(addresses: List<String>): Either<CallError, Unit> = either {
        withContext(Dispatchers.IO) {
            poolyClient.postOrEither<String>("$baseUrl/users/me/wallets") {
                contentType(ContentType.Application.Json)
                setBody(addresses)
            }.bind()
        }
    }
}
