package com.example.dajho

import com.example.dajho.util.toCentsLong
import com.example.dajho.util.toCurrencyString
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas unitarias para métricas y agregaciones del Dashboard (Fase 10).
 */
class DashboardUnitTest {

    @Test
    fun todaySalesAggregationTest() {
        val sale1Cents = 1250L // $12.50
        val sale2Cents = 2500L // $25.00
        val sale3Cents = 700L  // $7.00

        val totalSalesTodayCents = sale1Cents + sale2Cents + sale3Cents

        assertEquals(4450L, totalSalesTodayCents) // $44.50
        assertEquals("$44.50", totalSalesTodayCents.toCurrencyString())
    }

    @Test
    fun lowStockFilterCountTest() {
        val stockVariants = listOf(5, 8, 2, 1, 10, 0, 3)
        val lowStockCount = stockVariants.count { it <= 3 }

        assertEquals(4, lowStockCount) // Variants with stock <= 3: 2, 1, 0, 3
    }

    @Test
    fun debtorsCountAndTotalTest() {
        val clientDebtsCents = listOf(0L, 2850L, 1500L, 0L, 12000L) // $0, $28.50, $15.00, $0, $120.00

        val debtors = clientDebtsCents.filter { it > 0L }
        val totalDebtCents = debtors.sum()

        assertEquals(3, debtors.size)
        assertEquals(16350L, totalDebtCents) // $163.50
        assertEquals("$163.50", totalDebtCents.toCurrencyString())
    }
}
