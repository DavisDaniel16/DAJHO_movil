package com.example.dajho

import com.example.dajho.util.toCurrencyString
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas unitarias para formateo de reportes CSV y conversión de montos (Fase 11).
 */
class ReportsUnitTest {

    @Test
    fun csvRowFormattingTest() {
        val saleNumber = "000012"
        val method = "CASH"
        val totalCents = 1250L

        val totalStr = totalCents.toCurrencyString().replace("$", "")
        val csvLine = "\"$saleNumber\",\"$method\",\"$totalStr\""

        assertEquals("\"000012\",\"CASH\",\"12.50\"", csvLine)
    }

    @Test
    fun currencyStringFormattingTest() {
        val amountCents = 3500L
        assertEquals("$35.00", amountCents.toCurrencyString())
    }
}
