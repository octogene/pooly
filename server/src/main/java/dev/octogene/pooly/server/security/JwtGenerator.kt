package dev.octogene.pooly.server.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.serialization.Serializable
import kotlin.math.sign
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class JwtGenerator(
    private val secret: String
) {
    // TODO: Proper JWT generation
    fun createToken(username: String): Token {
        val expiration = (Clock.System.now() + 7.days)
        val token = JWT.create()
            .withAudience("audience")
            .withIssuer("issuer")
            .withClaim("username", username)
            .withExpiresAt(expiration.toJavaInstant())
            .sign(Algorithm.HMAC256(secret))

        return Token(
            token = token,
            expiration = expiration
        )
    }
}

@Serializable
data class Token(
    val token: String,
    val expiration: Instant
)
