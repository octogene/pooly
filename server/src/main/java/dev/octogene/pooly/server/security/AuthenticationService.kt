package dev.octogene.pooly.server.security

import arrow.core.Either
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.ensure
import arrow.core.raise.context.ensureNotNull
import dev.octogene.pooly.common.backend.security.PasswordHasher
import dev.octogene.pooly.common.backend.security.PasswordVerifier
import dev.octogene.pooly.common.db.repository.RepositoryError
import dev.octogene.pooly.common.db.repository.UserRepository
import dev.octogene.pooly.core.User
import dev.octogene.pooly.core.UserRole
import dev.octogene.pooly.server.model.ApplicationError.AuthenticationError
import dev.octogene.pooly.server.model.ApplicationError.AuthenticationError.InternalError
import dev.octogene.pooly.server.model.ApplicationError.AuthenticationError.InvalidCredentials
import dev.octogene.pooly.server.model.ApplicationError.AuthenticationError.UserAlreadyExists
import dev.octogene.pooly.server.model.ApplicationError.AuthenticationError.UserNotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AuthenticationService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val passwordVerifier: PasswordVerifier,
    private val jwtManager: JwtManager,
    private val logger: Logger = LoggerFactory.getLogger(AuthenticationService::class.java),
) {
    suspend fun login(username: String, plainPassword: String): Either<AuthenticationError, User> = either {
        val user = userRepository.findUserByUsername(username).mapLeft {
            UserNotFound(username)
        }.bind()

        ensure(
            passwordVerifier.verify(
                plain = plainPassword,
                hash = user.passwordHash,
            ),
        ) {
            InvalidCredentials
        }
        user
    }.onLeft {
        logger.error("Failed to login user {}", username)
    }

    suspend fun register(
        username: String,
        email: String,
        plainPassword: String,
        role: UserRole = UserRole.USER,
    ): Either<AuthenticationError, Unit> = either {
        val user = userRepository.findUserByUsername(username).getOrNull()
        ensure(user == null) { UserAlreadyExists(username) }
        val hashedPassword = passwordHasher.hash(plainPassword)
        return userRepository.createUser(username, email, hashedPassword, role)
            .mapLeft { error ->
                when (error) {
                    is RepositoryError.AlreadyExists -> UserAlreadyExists(username)
                    is RepositoryError.DatabaseError -> InternalError(error.message)
                    is RepositoryError.NotFound -> UserNotFound(error.identifier)
                }
            }
    }.onLeft {
        logger.error("Failed registration of user {}", username)
    }

    suspend fun removeUser(username: String): Either<AuthenticationError, Unit> = either {
        val user = userRepository.findUserByUsername(username).getOrNull()
        ensureNotNull(user) { UserNotFound(username) }
        return userRepository.removeUser(username)
            .mapLeft { error ->
                when (error) {
                    is RepositoryError.AlreadyExists -> UserAlreadyExists(username)
                    is RepositoryError.DatabaseError -> InternalError(error.message)
                    is RepositoryError.NotFound -> UserNotFound(error.identifier)
                }
            }
    }.onLeft {
        logger.error("Failed to remove user {}", username)
    }
    fun generateToken(user: User): Token = jwtManager.createToken(user.username, user.role)
}
