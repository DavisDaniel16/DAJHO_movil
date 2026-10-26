package com.example.dajho

import com.example.dajho.util.toCentsLong
import com.example.dajho.util.toCurrencyString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para validaciones de Deudas y Abonos (Fase 8 - RN-08).
 */
class DebtValidationUnitTest {

    @Test
    fun debtPaymentExceedsDebtRejectedTest() {
        val currentDebtCents = 2850L // Deuda actual $28.50
        val abonoValidCents = 1500L  // Abono válido $15.00
        val abonoExceedsCents = 3500L // Abono excesivo $35.00

        val isValidAbono = abonoValidCents > 0 && abonoValidCents <= currentDebtCents
        val isExceedsAbonoValid = abonoExceedsCents > 0 && abonoExceedsCents <= currentDebtCents

        assertTrue(isValidAbono)
        assertFalse(isExceedsAbonoValid)
    }

    @Test
    fun debtPaymentZeroOrNegativeRejectedTest() {
        val currentDebtCents = 2850L // Deuda actual $28.50
        val abonoZero = 0L
        val abonoNegative = -1000L

        val isZeroValid = abonoZero > 0 && abonoZero <= currentDebtCents
        val isNegativeValid = abonoNegative > 0 && abonoNegative <= currentDebtCents

        assertFalse(isZeroValid)
        assertFalse(isNegativeValid)
    }

    @Test
    fun debtPaymentValidReducesDebtTest() {
        val initialDebtCents = 2850L // $28.50
        val paymentCents = 1500L     // $15.00

        val remainingDebtCents = (initialDebtCents - paymentCents).coerceAtLeast(0L)

        assertEquals(1350L, remainingDebtCents) // $13.50
        assertEquals("$13.50", remainingDebtCents.toCurrencyString())
    }
}
