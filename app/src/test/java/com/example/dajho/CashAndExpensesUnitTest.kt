package com.example.dajho

import com.example.dajho.util.toCurrencyString
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas unitarias para Arqueo de Caja, Saldo Esperado y Gastos (Fase 17).
 */
class CashAndExpensesUnitTest {

    @Test
    fun expectedCashFormulaTest() {
        val initialCashCents = 2000L    // $20.00
        val cashSalesCents = 12250L     // $122.50
        val collectedDebtsCents = 2000L // $20.00
        val totalExpensesCents = 2500L  // $25.00

        val expectedCashCents = initialCashCents + cashSalesCents + collectedDebtsCents - totalExpensesCents

        assertEquals(13750L, expectedCashCents) // $137.50
        assertEquals("$137.50", expectedCashCents.toCurrencyString())
    }

    @Test
    fun cashShiftDifferenceCalculationTest() {
        val expectedCashCents = 13750L // $137.50
        val countedCashSurplusCents = 14000L // $140.00
        val countedCashShortCents = 13000L   // $130.00

        val surplusCents = countedCashSurplusCents - expectedCashCents
        val shortCents = countedCashShortCents - expectedCashCents

        assertEquals(250L, surplusCents)   // Sobrante +$2.50
        assertEquals(-750L, shortCents)    // Faltante -$7.50
    }
}
