package dev.octogene.pooly.server.prize

import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.raise.ktor.server.response.Response
import arrow.raise.ktor.server.response.Response.Companion.Response
import dev.octogene.pooly.common.cache.CacheClient
import dev.octogene.pooly.common.cache.get
import dev.octogene.pooly.common.cache.set
import dev.octogene.pooly.common.db.repository.PageRequest
import dev.octogene.pooly.common.db.repository.PrizeRepository
import dev.octogene.pooly.common.db.repository.UserRepository
import dev.octogene.pooly.common.db.repository.WalletRepository
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.server.getNextDayAt
import dev.octogene.pooly.server.model.mapToResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import kotlinx.datetime.LocalTime
import kotlin.time.Instant

class PrizeController(
    private val cacheClient: CacheClient,
    private val prizeRepository: PrizeRepository,
    private val walletRepository: WalletRepository,
    private val userRepository: UserRepository,
    private val defaultCacheExpirationAt: Instant = getNextDayAt(LocalTime(21, 0))
) {
    context(_: Raise<Response>)
    suspend fun getAllPrizes(username: String): Response {
        val user = userRepository.findUserByUsername(username).mapLeft {
            Response(Unauthorized, "Unknown user, credentials must be outdated")
        }.bind()
        val wallets = walletRepository.getWalletsForUser(user.username).mapLeft {
            Response(HttpStatusCode.Forbidden)
        }.bind()
        val cacheKey = "allPrizes-$username"
        return cacheClient.get<List<Prize>>(cacheKey)
            .fold(
                ifEmpty = {
                    prizeRepository.getAllPrizes(wallets)
                        .map { prizes ->
                            // Since draws run daily, we can have a long and precise expiration
                            if (prizes.isNotEmpty()) {
                                cacheClient.set(
                                    cacheKey,
                                    prizes,
                                    defaultCacheExpirationAt
                                )
                            }
                            Response(HttpStatusCode.OK, prizes)
                        }.mapLeft(::mapToResponse).bind()
                },
                ifSome = {
                    Response(HttpStatusCode.OK, it)
                })
    }

    context(_: Raise<Response>)
    suspend fun getAllPrizesByPage(
        username: String,
        page: Int,
        pageSize: Int?
    ): Response {
        val user = userRepository.findUserByUsername(username).mapLeft {
            Response(Unauthorized, "Unknown user, credentials must be outdated")
        }.bind()
        val wallets = walletRepository.getWalletsForUser(user.username).mapLeft {
            Response(HttpStatusCode.Forbidden)
        }.bind()
        val pageRequest = PageRequest.create(page, pageSize).mapLeft {
            Response(HttpStatusCode.BadRequest, it)
        }.bind()
        return prizeRepository.getAllPrizesPaged(wallets, pageRequest)
            .map { Response(HttpStatusCode.OK, it) }
            .mapLeft(::mapToResponse).bind()
    }
}