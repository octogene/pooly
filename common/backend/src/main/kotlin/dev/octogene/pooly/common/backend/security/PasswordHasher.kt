package dev.octogene.pooly.common.backend.security

import dev.octogene.pooly.core.PasswordHash

interface PasswordHasher {
    /**
     * Hashes a plain-text password using Argon2.
     *
     * @param password The plain-text password to hash.
     * @return The hashed password.
     */
    fun hash(password: String): PasswordHash
}

