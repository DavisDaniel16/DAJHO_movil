package com.example.dajho.util

import android.util.Patterns
import java.util.UUID

/**
 * Utilidades de validación y sanitización estricta para DAJHO (Fase 9).
 */
object ValidationUtils {

    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    fun isValidPhone(phone: String?): Boolean {
        if (phone.isNullOrBlank()) return true // opcional o validado
        val trimmed = phone.trim()
        return trimmed.length in 7..15 && trimmed.all { it.isDigit() || it == '+' || it == '-' || it.isWhitespace() }
    }

    fun isValidRucOrCedula(value: String?): Boolean {
        if (value.isNullOrBlank()) return true
        val trimmed = value.trim()
        return trimmed.length in 10..13 && trimmed.all { it.isDigit() }
    }

    fun isValidUuid(uuid: String?): Boolean {
        if (uuid.isNullOrBlank()) return false
        return try {
            UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun sanitizeText(input: String?): String {
        if (input.isNullOrBlank()) return ""
        // Eliminar caracteres peligrosos o scripts HTML básicos
        return input.trim()
            .replace("<script>", "", ignoreCase = true)
            .replace("</script>", "", ignoreCase = true)
            .replace("javascript:", "", ignoreCase = true)
    }

    fun validatePrice(priceCentavos: Long): Boolean {
        return priceCentavos >= 0
    }

    fun validateQuantity(quantity: Int): Boolean {
        return quantity > 0
    }
}
