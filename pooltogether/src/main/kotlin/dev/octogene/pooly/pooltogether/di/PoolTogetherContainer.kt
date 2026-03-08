package dev.octogene.pooly.pooltogether.di

import dev.octogene.pooly.pooltogether.PoolTogetherRepository
import dev.octogene.pooly.pooltogether.PoolyApiClient
import dev.octogene.pooly.pooltogether.PoolyApiClientImpl
import dev.octogene.pooly.pooltogether.db.DrawQueries
import dev.octogene.pooly.pooltogether.db.VaultQueries
import dev.octogene.pooly.settings.db.WalletQueries
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json

@ContributesTo(AppScope::class)
@BindingContainer
class PoolTogetherContainer {

    @Provides
    fun providePoolTogetherRepository(
        client: PoolyApiClient,
        walletQueries: WalletQueries,
        drawQueries: DrawQueries,
        vaultQueries: VaultQueries,
    ): PoolTogetherRepository = PoolTogetherRepository(client, walletQueries, vaultQueries, drawQueries)

    @Provides
    fun providesPoolyApiClient(drawQueries: DrawQueries, httpClient: HttpClient): PoolyApiClient = PoolyApiClientImpl(
        drawQueries = drawQueries,
        httpClient = httpClient,
    )

    @Provides
    fun provideHttpClient(): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
    }
}
