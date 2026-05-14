package dev.octogene.pooly.server.test

import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.server.security.Token
import dev.octogene.pooly.server.user.RegisterUserRequest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.take
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import prize
import kotlin.test.Test
import kotlin.test.assertEquals

class PrizesRouteTest {

    @Test
    fun `GET prizes should return all prizes`() = testApplication {
        application {
            testApp(
                testAppConfig,
                listOf(RegisterUserRequest("testuser", "password", "test@example.com")),
                Arb.prize().take(2).toList(),
            )
        }
        val client = createClient { install(ContentNegotiation) { json() } }

        val loginResponse =
            client.loginUser("testuser", "password").body<Token>()

        val prizesResponse = client.get("/api/v1/users/me/prizes") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${loginResponse.token}")
        }

        assertEquals(HttpStatusCode.OK, prizesResponse.status)
        val prizes = prizesResponse.body<List<Prize>>()
        assertEquals(2, prizes.size)
    }
}
