package dev.octogene.pooly.server.test

import dev.octogene.pooly.core.Address
import dev.octogene.pooly.core.Amount
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.core.Vault
import dev.octogene.pooly.server.security.Token
import dev.octogene.pooly.server.user.RegisterUserRequest
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
import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

class PrizesRouteTest {

    @Test
    fun `GET prizes should return all prizes`() = testApplication {
        application {
            testApp(
                testAppConfig,
                listOf(RegisterUserRequest("testuser", "password", "test@example.com")),
                listOf(
                    Prize(
                        payout = Amount.from("223338444"),
                        timestamp = Clock.System.now(),
                        winner = Address.unsafeFrom("0x4d864b0ddec2a861506c8baa676e2d99a4c30a84"),
                        vault = Vault(
                            address = Address.unsafeFrom("0x7d864b0ddec2a861506c8baa676e2d99a4c30a84"),
                            name = "TestVault",
                            symbol = "TST",
                            decimals = 18,
                            network = ChainNetwork.BASE,
                        ),
                        transactionHash = "0x111",
                    ),
                ),
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
        assertEquals(1, prizes.size)
    }
}
