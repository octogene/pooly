package dev.octogene.pooly.server.admin

import arrow.core.raise.Raise
import arrow.raise.ktor.server.response.Response
import dev.octogene.pooly.common.cache.CacheClient
import io.ktor.http.HttpStatusCode

class AdminController(
    private val cacheClient: CacheClient
) {
    context(_: Raise<Response>)
    suspend fun clearCache(request: ClearCacheRequest): Response {
        cacheClient.clearByPattern(request.pattern).mapLeft {
            Response.Response(HttpStatusCode.InternalServerError)
        }
        return Response.Response(HttpStatusCode.OK)
    }
}