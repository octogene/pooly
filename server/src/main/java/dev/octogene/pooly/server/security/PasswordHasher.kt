package dev.octogene.pooly.server.security

import de.mkammerer.argon2.Argon2Factory
import de.mkammerer.argon2.Argon2Factory.Argon2Types
import dev.octogene.pooly.common.backend.security.PasswordHasher
import dev.octogene.pooly.common.backend.security.PasswordVerifier
import dev.octogene.pooly.core.PasswordHash

class Argon2PasswordHasher(
    argon2Type: String,
    private val iterations: Int,
    private val memory: Int,
    private val parallelism: Int,
) : PasswordHasher,
    PasswordVerifier {
    private val argon2 = Argon2Factory.create(getType(argon2Type))

    override fun hash(password: String): PasswordHash {
        val passwordBytes = password.toByteArray()
        val hash = try {
            argon2.hash(iterations, memory, parallelism, passwordBytes)
        } finally {
            argon2.wipeArray(passwordBytes)
        }
        return PasswordHash(hash)
    }

    override fun verify(plain: String, hash: PasswordHash): Boolean = try {
        argon2.verify(hash.content, plain.toByteArray())
    } catch (e: Exception) {
        false
    }

    private fun getType(type: String): Argon2Types = Argon2Types.entries.find { it.name == type }
        ?: Argon2Types.ARGON2id
}
