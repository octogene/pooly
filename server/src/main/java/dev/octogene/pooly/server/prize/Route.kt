package dev.octogene.pooly.server.prize

import arrow.raise.ktor.server.routing.getOrRaise
import com.sun.beans.introspect.PropertyInfo
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.server.model.ApiError
import dev.octogene.pooly.server.user.getUsername
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.openapi.ReferenceOr.Companion.schema
import io.ktor.openapi.jsonSchema
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.header
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyles
import org.koin.ktor.ext.inject

@OptIn(ExperimentalKtorApi::class)
fun Route.prizesRoute() {
    val prizeController: PrizeController by inject()

    authenticate("auth-jwt") {
        getOrRaise("/prizes") {
            val username = getUsername()
            val page = call.queryParameters["page"]?.toInt()
            val pageSize = call.queryParameters["pagesSize"]?.toInt()
            if (page != null) {
                application.log.debug("Getting prizes by page {} with page size {}", page, pageSize)
                prizeController.getAllPrizesByPage(username, page, pageSize)
            } else prizeController.getAllPrizes(username)
        }.describe {
            summary = "All prizes"
            description = "Retrieve all prizes for the current user"
            requestBody {
                header(HttpHeaders.Authorization, "Bearer [JWT Token]") {
                    description = "JWT Token"
                    required = true
                }
                header(
                    HttpHeaders.IfModifiedSince,
                    "<day-name>, <day> <month> <year> <hour>:<minute>:<second> GMT"
                ) {
                    description = "If-Modified-Since"
                    required = false
                }
            }
            parameters {
                // TODO: Check for conditional fields
                query("page") {
                    description = "Page number"
                    required = false
                }
                query("pageSize") {
                    description = "Page size"
                    required = false
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "A list of prizes"
                    schema = jsonSchema<List<Prize>>()
                }
                HttpStatusCode.Unauthorized {
                    description = "Unauthorized"
                    schema = jsonSchema<ApiError>()
                }
                HttpStatusCode.BadRequest {
                    description = "Bad Request"
                    schema = jsonSchema<ApiError>()
                }
                HttpStatusCode.InternalServerError {
                    description = "Internal Server Error"
                    schema = jsonSchema<ApiError>()
                }
                HttpStatusCode.NotModified {
                    description = "Not Modified"
                    schema = jsonSchema<ApiError>()
                }

            }
        }
    }
}