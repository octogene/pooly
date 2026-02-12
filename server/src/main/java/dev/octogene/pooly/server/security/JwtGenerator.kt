package dev.octogene.pooly.server.security

import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import dev.octogene.pooly.core.UserRole
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class JwtManager(
    private val algorithm: Algorithm,
    private val creator: JWTCreator.Builder,
    private val verifier: JWTVerifier,
    private val expiration: Duration = 7.days,
) : JWTVerifier by verifier {

    // TODO: Proper JWT generation
    fun createToken(username: String, role: UserRole): Token {
        val expiration = (Clock.System.now() + expiration)
        val token = creator
            .withExpiresAt(expiration.toJavaInstant())
            .withClaim("role", role.name)
            .withClaim("username", username)
            .sign(algorithm)

        return Token(
            token = token,
            expiration = expiration,
        )
    }
}

@Serializable
data class Token(val token: String, val expiration: Instant)
