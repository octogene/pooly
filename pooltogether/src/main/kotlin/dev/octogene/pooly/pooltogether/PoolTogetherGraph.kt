package dev.octogene.pooly.pooltogether

import dev.octogene.pooly.pooltogether.db.DrawQueries
import dev.octogene.pooly.ptgraph.api.PoolTogetherGraphQLClient
import dev.octogene.pooly.rpc.PoolTogetherRPCClient
import dev.octogene.pooly.settings.db.WalletQueries
import dev.octogene.pooly.shared.model.ChainNetwork
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

abstract class PoolTogetherGraphScope()

@ContributesTo(AppScope::class)
@BindingContainer
class PoolTogetherGraph {

    @Provides
    fun providePoolTogetherRepository(
        client: PoolTogetherClient,
        walletQueries: WalletQueries,
        drawQueries: DrawQueries
    ): PoolTogetherRepository = PoolTogetherRepository(client, walletQueries, drawQueries)

    @Provides
    fun providesPoolTogetherClient(
        graphClient: PoolTogetherGraphQLClient,
        rpcClient: PoolTogetherRPCClient,
        drawQueries: DrawQueries
    ): PoolTogetherClient = PoolTogetherClient(
        graphClient = graphClient,
        rpcClient = rpcClient,
        drawQueries = drawQueries
    )

    @Provides
    fun providesPoolTogetherGraphQLClient(): PoolTogetherGraphQLClient = PoolTogetherGraphQLClient(ChainNetwork.entries)

    @Provides
    fun providesPoolTogetherRPCClient(): PoolTogetherRPCClient = PoolTogetherRPCClient()
}
