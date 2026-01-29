package dev.octogene.pooly.server.security

import de.mkammerer.argon2.Argon2Factory
import de.mkammerer.argon2.Argon2Factory.Argon2Types

interface PasswordHasher {
    fun hash(password: String): String
    fun verify(plainPassword: String, hashedPassword: String): Boolean
}

class Argon2PasswordHasher(
    argon2Type: String,
    private val iterations: Int,
    private val memory: Int,
    private val parallelism: Int
): PasswordHasher {
    private val argon2 = Argon2Factory.create(getType(argon2Type))

    override fun hash(password: String): String {
        val passwordBytes = password.toByteArray()
        val hash = try {
            argon2.hash(iterations, memory, parallelism, passwordBytes)
        } finally {
            argon2.wipeArray(passwordBytes)
        }
        return hash
    }

    override fun verify(plainPassword: String, hashedPassword: String): Boolean {
        return try {
            argon2.verify(hashedPassword, plainPassword.toByteArray())
        } catch (e: Exception) {
            false
        }
    }

    private fun getType(type: String): Argon2Types {
        return Argon2Types.entries.find { it.name == type }
            ?: Argon2Types.ARGON2id
    }
}
