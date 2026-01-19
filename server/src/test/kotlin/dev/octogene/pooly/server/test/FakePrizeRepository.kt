package dev.octogene.pooly.server.test

import arrow.core.Either
import dev.octogene.pooly.common.db.repository.PrizeRepository
import dev.octogene.pooly.common.db.repository.RepositoryError
import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.Prize

class FakePrizeRepository(
    private val prizes: List<Prize> = emptyList()
): PrizeRepository {
    override suspend fun insertPrizes(prizes: List<Prize>): Either<RepositoryError, Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllPrizes(wallets: List<Address>): Either<RepositoryError.DatabaseError, List<Prize>> {
        return Either.Right(prizes)
    }
}