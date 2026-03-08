package dev.octogene.pooly.rpc

import dev.octogene.pooly.common.core.BuildConfig.ALCHEMY_KEY
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.core.Vault
import io.ethers.providers.Provider
import java.math.BigInteger
import io.ethers.core.types.Address as EthersAddress

class PoolTogetherRPCClient(rpcUrl: String = "https://base-mainnet.g.alchemy.com/v2/$ALCHEMY_KEY") {
    // TODO: Initialization without a chainId can cause an exception since it calls
    //  directly the chain to fetch an id
    private val provider = Provider.fromUrl(rpcUrl, chainId = ChainNetwork.BASE.id).unwrap()

    fun getVaultInfoFromAdresses(addresses: List<String>): List<Vault> {
        val currentBlockNumber = provider.getBlockNumber().sendAwait().unwrap()

        return addresses.map { hexAddress ->
            val address = EthersAddress.fromHex(hexAddress).unwrap()
            val vaults = Vaults(provider, address)
            val (name, symbol, decimals) = listOf(
                vaults.name(),
                vaults.symbol(),
                vaults.decimals(),
            ).map { functionCall ->
                // TODO: make calls async or use multi-call
                functionCall.call(currentBlockNumber).sendAwait().unwrap()
            }
            Vault(
                Address.unsafeFrom(hexAddress),
                name as String,
                symbol as String,
                // TODO: Check why BigInteger is returned by contract. Really not required.
                (decimals as BigInteger).toInt(),
                ChainNetwork.BASE,
            )
        }
    }
}
