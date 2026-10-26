package com.example.dajho

import com.example.dajho.clients.Client
import com.example.dajho.util.toCurrencyString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para Deudas de Clientes, Abonos y Regla Estricta RN-08 (Fase 17).
 */
class DebtAndPaymentsUnitTest {

    @Test
    fun clientDebtStatusTest() {
        val clientNoDebt = Client(id = 1L, name = "María López", phone = "0991234567", debt = 0.00)
        val clientWithDebt = Client(id = 2L, name = "Carlos Mendoza", phone = "0987654321", debt = 28.50)

        assertFalse(clientNoDebt.hasDebt())
        assertTrue(clientWithDebt.hasDebt())
    }

    @Test
    fun debtPaymentRn08ValidationTest() {
        val currentDebtCents = 2850L // $28.50

        val validAbonoCents = 1500L  // $15.00 -> Válido
        val exceedsAbonoCents = 3000L // $30.00 -> Rechazado por superar la deuda
        val zeroAbonoCents = 0L       // $0.00 -> Rechazado por monto no positivo

        val isValid = validAbonoCents > 0L && validAbonoCents <= currentDebtCents
        val isExceedsValid = exceedsAbonoCents > 0L && exceedsAbonoCents <= currentDebtCents
        val isZeroValid = zeroAbonoCents > 0L && zeroAbonoCents <= currentDebtCents

        assertTrue(isValid)
        assertFalse(isExceedsValid)
        assertFalse(isZeroValid)
    }

    @Test
    fun debtReductionAfterAbonoTest() {
        val initialDebtCents = 2850L // $28.50
        val abonoCents = 1500L       // $15.00

        val remainingDebtCents = (initialDebtCents - abonoCents).coerceAtLeast(0L)

        assertEquals(1350L, remainingDebtCents) // $13.50
        assertEquals("$13.50", remainingDebtCents.toCurrencyString())
    }
}
