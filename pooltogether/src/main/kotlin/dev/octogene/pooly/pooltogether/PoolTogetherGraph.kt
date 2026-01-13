package dev.octogene.pooly.pooltogether

import dev.octogene.pooly.pooltogether.db.DrawQueries
import dev.octogene.pooly.settings.db.WalletQueries
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
@BindingContainer
class PoolTogetherGraph {

    @Provides
    fun providePoolTogetherRepository(
        client: PoolyApiClientImpl,
        walletQueries: WalletQueries,
        drawQueries: DrawQueries
    ): PoolTogetherRepository = PoolTogetherRepository(client, walletQueries, drawQueries)

    @Provides
    fun providesPoolyApiClient(
        drawQueries: DrawQueries
    ): PoolyApiClient = PoolyApiClientImpl(
        drawQueries = drawQueries
    )
}
