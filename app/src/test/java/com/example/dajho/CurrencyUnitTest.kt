package com.example.dajho

import com.example.dajho.util.toCentsLong
import com.example.dajho.util.toCurrencyString
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Prueba unitaria local para conversión de monedas y céntimos (Fase 2).
 */
class CurrencyUnitTest {

    @Test
    fun currencyConversionHelperTest() {
        val cents = 14250L // $142.50
        assertEquals("$142.50", cents.toCurrencyString())

        val doubleVal = 142.50
        assertEquals(14250L, doubleVal.toCentsLong())

        val stringVal = "$28.50"
        assertEquals(2850L, stringVal.toCentsLong())
    }
}
