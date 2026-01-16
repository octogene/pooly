package dev.octogene.pooly.server.test

import arrow.core.Either
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.server.model.DatabaseError
import dev.octogene.pooly.server.prize.PrizeRepository

class FakePrizeRepository(
    private val prizes: List<Prize> = emptyList()
): PrizeRepository {

    override suspend fun getAllPrizes(wallets: List<Address>): Either<DatabaseError, List<Prize>> {
        return Either.Right(prizes)
    }

    override suspend fun getLatestPrizes(wallets: List<Address>): Either<DatabaseError, List<Prize>> {
        TODO("Not yet implemented")
    }
}