package dev.octogene.pooly.rpc

import dev.octogene.pooly.shared.BuildConfig.ALCHEMY_KEY
import io.ethers.core.types.Address
import io.ethers.providers.Provider
import java.math.BigInteger

class PoolTogetherRPCClient(
    private val rpcUrl: String = "https://base-mainnet.g.alchemy.com/v2/$ALCHEMY_KEY"
) {
    private val provider = Provider.fromUrl(rpcUrl).unwrap()

    fun getVaultInfoFromAdresses(addresses: List<String>): List<RpcVault> {
        val currentBlockNumber = provider.getBlockNumber().sendAwait().unwrap()

        val vaultNames = addresses.distinct().flatMap {
            val address = Address.fromHex(it).unwrap()
            val vaults = Vaults(provider, address)
            listOf(
                vaults.name(),
                vaults.symbol(),
                vaults.decimals()
            )
        }.map {
            it.call(currentBlockNumber).sendAwait().unwrap()
        }.chunked(3).map { (name, symbol, decimals) ->
            RpcVault("", name as String, symbol as String, decimals as BigInteger)
        }

        return vaultNames
    }
}
