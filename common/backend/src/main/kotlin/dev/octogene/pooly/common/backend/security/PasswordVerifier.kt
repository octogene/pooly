package dev.octogene.pooly.common.backend.security

import dev.octogene.pooly.core.PasswordHash

interface PasswordVerifier {
    /**
     * Verifies a given plain-text password against a hashed password.
     *
     * @param plain The plain-text password to verify.
     * @param hash The hashed password to compare against.
     * @return True if the password is correct, false otherwise.
     */
    fun verify(plain: String, hash: PasswordHash): Boolean
}