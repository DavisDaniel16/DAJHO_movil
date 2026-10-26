package com.example.dajho.util

import java.security.MessageDigest

/**
 * Utilidad para hashing seguro de contraseñas con SHA-256 (Fase 5).
 * Nunca guarda contraseñas en texto plano.
 */
object PasswordUtils {

    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return hashPassword(password) == storedHash
    }
}
