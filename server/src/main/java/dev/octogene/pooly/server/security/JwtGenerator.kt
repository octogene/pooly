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
    private val refreshExpiration: Duration = 30.days,
) : JWTVerifier by verifier {

    companion object {
        private const val USERNAME_CLAIM_NAME = "username"
        private const val ROLE_CLAIM_NAME = "role"
        private const val REFRESH_CLAIM_NAME = "refresh"
    }

    fun createToken(username: String, role: UserRole): Token {
        val now = Clock.System.now()
        val tokenExpiration = now + expiration
        val refreshTokenExpiration = now + refreshExpiration

        val token = creator
            .withExpiresAt(tokenExpiration.toJavaInstant())
            .withClaim(ROLE_CLAIM_NAME, role.name)
            .withClaim(USERNAME_CLAIM_NAME, username)
            .sign(algorithm)

        // TODO: store token & implement PKCE
        val refreshToken = creator
            .withExpiresAt(refreshTokenExpiration.toJavaInstant())
            .withClaim(USERNAME_CLAIM_NAME, username)
            .withClaim(REFRESH_CLAIM_NAME, true)
            .sign(algorithm)

        return Token(
            token = token,
            expiration = tokenExpiration,
            refreshToken = refreshToken,
        )
    }
}

@Serializable
data class Token(val token: String, val expiration: Instant, val refreshToken: String? = null)
