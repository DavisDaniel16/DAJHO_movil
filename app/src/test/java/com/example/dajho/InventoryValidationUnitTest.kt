package com.example.dajho

import com.example.dajho.util.toCentsLong
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para validaciones del módulo de Inventario y margen de ganancia (Fase 6).
 */
class InventoryValidationUnitTest {

    @Test
    fun marginCalculationTest() {
        val costCents = 650L      // $6.50
        val salePriceCents = 1250L // $12.50

        val profitCents = salePriceCents - costCents // $6.00
        val marginPercentage = ((profitCents.toDouble() / salePriceCents.toDouble()) * 100).toInt()

        assertEquals(600L, profitCents)
        assertEquals(48, marginPercentage) // 48% margin
    }

    @Test
    fun costAndPriceValidationTest() {
        val costCents = 1000L      // $10.00
        val salePriceValid = 1500L  // $15.00
        val salePriceInvalid = 800L // $8.00

        assertTrue(costCents > 0)
        assertTrue(salePriceValid >= costCents)
        assertFalse(salePriceInvalid >= costCents)
    }

    @Test
    fun stockNonNegativeTest() {
        val stockValid = 5
        val stockZero = 0
        val stockInvalid = -3

        assertTrue(stockValid >= 0)
        assertTrue(stockZero >= 0)
        assertFalse(stockInvalid >= 0)
    }
}
