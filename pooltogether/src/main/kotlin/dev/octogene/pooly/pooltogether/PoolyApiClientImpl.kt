package dev.octogene.pooly.pooltogether

import android.R.attr.password
import arrow.core.Either
import arrow.core.raise.either
import co.touchlab.kermit.Logger
import dev.octogene.pooly.common.mobile.CredentialRepository
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import dev.octogene.pooly.pooltogether.model.CallError
import dev.octogene.pooly.pooltogether.model.CreateUserRequest
import dev.octogene.pooly.pooltogether.model.LoginRequest
import dev.octogene.pooly.pooltogether.model.LoginResponse
import dev.octogene.pooly.pooltogether.model.getOrEither
import dev.octogene.pooly.pooltogether.model.postOrEither
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.accept
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Inject
internal class PoolyApiClientImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String = BuildConfig.POOLY_BASE_URL,
    private val credentialRepository: CredentialRepository,
) : PoolyApiClient {

    private val poolyClient: HttpClient = httpClient.config {
        install(Auth) {
            bearer {
                loadTokens {
                    credentialRepository.getToken()?.let { (val token = first, val refreshToken = second) ->
                        BearerTokens(token, refreshToken)
                    }
                }
                refreshTokens {
                    credentialRepository.getLogin()?.let { (val username = first, val password = second) ->
                        loginUser(username, password).getOrNull()
                            ?.let { (token, expiration, refreshToken) ->
                                credentialRepository.setToken(token, refreshToken)
                                BearerTokens(token, refreshToken)
                            }
                    }
                }
            }
        }
    }

    override suspend fun loginUser(username: String, password: String): Either<CallError, LoginResponse> = either {
        httpClient.postOrEither<LoginResponse>("$baseUrl/auth/tokens") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }.onLeft {
            Logger.e { "Failed to log user: $it" }
        }.bind()
    }

    override suspend fun registerUser(username: String, password: String, email: String): Either<CallError, Unit> =
        either {
            poolyClient.postOrEither<String>("$baseUrl/users") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(CreateUserRequest(username, password, email))
            }.onLeft {
                Logger.e { "Failed to register user: $it" }
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

    override suspend fun getWallets(): Either<CallError, List<String>> = either {
        withContext(Dispatchers.IO) {
            poolyClient.getOrEither<List<String>>("$baseUrl/users/me/wallets") {
                contentType(ContentType.Application.Json)
            }.bind()
        }
    }
}
