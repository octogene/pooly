package dev.octogene.pooly.server.test

import dev.octogene.pooly.server.config.AppConfig
import dev.octogene.pooly.server.config.DbConfig
import dev.octogene.pooly.server.config.HashingConfig
import dev.octogene.pooly.server.config.JwtConfig
import dev.octogene.pooly.server.config.Metrics
import dev.octogene.pooly.server.config.SecurityConfig
import dev.octogene.pooly.server.security.Token
import dev.octogene.pooly.server.user.User
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals


class UsersRouteTest {

    @Test
    fun `POST users should create a new user`() = testApplication {
        application {
            testApp(testAppConfig)
        }
        val newUserJson = """
            {
                "username": "testuser",
                "password": "password"
                "email": "test@example.com"
            }
        """.trimIndent()

        val response = client.post("/api/v1/register") {
            contentType(ContentType.Application.Json)
            setBody(newUserJson)
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `POST invalid wallets should return a Bad Request`() = testApplication {
        application {
            testApp(
                testAppConfig,
                listOf(User("testuser", "password", "test@example.com"))
            )
        }
        val client = createClient { install(ContentNegotiation) { json() } }

        val credentialsJson = """
            {
                "username": "testuser",
                "password": "password"
            }
        """.trimIndent()

        val loginResponse = client.post("/api/v1/login") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(credentialsJson)
        }.body<Token>()

        val invalidWalletsJson = """
            [
               "0x1234567890abcdef1234567890abcdef123456",
               "0x1234567891abcdef1234567890abcdef123456" 
            ]
        """.trimIndent()

        val walletsResponse = client.post("/api/v1/wallets") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${loginResponse.token}")
            setBody(invalidWalletsJson)
        }

        assertEquals(HttpStatusCode.BadRequest, walletsResponse.status)
    }

    @Test
    fun `POST wallets should add wallets to user`() = testApplication {
        application {
            testApp(
                testAppConfig,
                listOf(User("testuser", "password", "test@example.com"))
            )
        }
        val client = createClient { install(ContentNegotiation) { json() } }

        val credentialsJson = """
            {
                "username": "testuser",
                "password": "password"
            }
        """.trimIndent()

        val loginResponse = client.post("/api/v1/login") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(credentialsJson)
        }.body<Token>()

        val walletsJson = """
            [
               "0x4d864b0ddec2a861506c8baa676e2d99a4c30a84" 
            ]
        """.trimIndent()

        val walletsResponse = client.post("/api/v1/wallets") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${loginResponse.token}")
            setBody(walletsJson)
        }

        assertEquals(HttpStatusCode.Created, walletsResponse.status)
    }

}