package com.example.dajho

import com.example.dajho.util.toCentsLong
import com.example.dajho.util.toCurrencyString
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas unitarias para cálculo de Arqueo, Balance y Saldo Esperado en Caja (Fase 9).
 */
class CashModuleUnitTest {

    @Test
    fun expectedCashCalculationTest() {
        val initialCashCents = 2000L    // $20.00
        val cashSalesCents = 10000L     // $100.00
        val collectedDebtsCents = 1500L // $15.00
        val totalExpensesCents = 2500L  // $25.00

        val expectedCashCents = initialCashCents + cashSalesCents + collectedDebtsCents - totalExpensesCents

        assertEquals(11000L, expectedCashCents) // $110.00
        assertEquals("$110.00", expectedCashCents.toCurrencyString())
    }

    @Test
    fun countedCashDifferenceTest() {
        val expectedCashCents = 11000L // $110.00
        val countedExactCents = 11000L // $110.00
        val countedShortCents = 10000L // $100.00
        val countedSurplusCents = 12000L // $120.00

        val diffExact = countedExactCents - expectedCashCents
        val diffShort = countedShortCents - expectedCashCents
        val diffSurplus = countedSurplusCents - expectedCashCents

        assertEquals(0L, diffExact)
        assertEquals(-1000L, diffShort)  // Faltante -$10.00
        assertEquals(1000L, diffSurplus) // Sobrante +$10.00
    }

    @Test
    fun nonCashSalesIsolationTest() {
        val initialCashCents = 2000L // $20.00
        val cashSalesCents = 5000L  // $50.00 en efectivo

        val transferSalesCents = 4000L // $40.00 en transferencia (No va a caja física)
        val creditSalesCents = 3000L   // $30.00 a crédito/fiado (No va a caja física)

        // Saldo Físico Esperado en Cajón sólo considera efectivo
        val physicalCashDrawerCents = initialCashCents + cashSalesCents

        assertEquals(7000L, physicalCashDrawerCents) // $70.00
        assertEquals("$70.00", physicalCashDrawerCents.toCurrencyString())

        // Balance Comercial Total considera todas las ventas
        val totalCommercialBalanceCents = initialCashCents + cashSalesCents + transferSalesCents + creditSalesCents
        assertEquals(14000L, totalCommercialBalanceCents) // $140.00
    }
}
