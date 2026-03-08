package dev.octogene.pooly.pooltogether

import arrow.core.Either
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import dev.octogene.pooly.pooltogether.model.CallError

interface PoolyApiClient {
    suspend fun registerUser(username: String, password: String, email: String): Either<CallError, Unit>

    suspend fun registerWallets(addresses: List<String>): Either<CallError, Unit>
    suspend fun getAllDraws(address: List<String>, network: ChainNetwork): Either<CallError, List<Prize>>
    suspend fun getVaultsInfo(addresses: List<String>): Either<CallError, List<Vault>>
}
