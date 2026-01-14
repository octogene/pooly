package dev.octogene.pooly.server.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaInstant

class JwtGenerator(
    private val secret: String
) {
    // TODO: Proper JWT generation
    fun createToken(username: String): String {
        return JWT.create()
            .withAudience("audience")
            .withIssuer("issuer")
            .withClaim("username", username)
            .withExpiresAt((Clock.System.now() + 7.days).toJavaInstant())
            .sign(Algorithm.HMAC256(secret))
    }
}
