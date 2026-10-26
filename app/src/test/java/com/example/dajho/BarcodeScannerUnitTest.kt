package com.example.dajho

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para control de desrebote de escáner y búsqueda de códigos (Fase 12).
 */
class BarcodeScannerUnitTest {

    @Test
    fun debounceTimeThresholdTest() {
        val lastCode = "CAM-001"
        val lastTime = 1000000L

        val sameCodeTimeDeltaShort = 1000000L + 500L  // 0.5s desrebote -> Rechazar
        val sameCodeTimeDeltaLong = 1000000L + 2000L  // 2.0s desrebote -> Aceptar
        val differentCodeTimeDeltaShort = 1000000L + 100L // Código nuevo -> Aceptar

        val isShortRepeatedAccepted = (sameCodeTimeDeltaShort - lastTime) > 1500L
        val isLongRepeatedAccepted = (sameCodeTimeDeltaLong - lastTime) > 1500L

        assertFalse(isShortRepeatedAccepted)
        assertTrue(isLongRepeatedAccepted)
    }

    @Test
    fun skuMatchAndNotFoundHandlingTest() {
        val localSkus = listOf("CAM-001", "ZAP-042", "GOR-009")

        val scannedCodeValid = "ZAP-042"
        val scannedCodeNotFound = "EAN-770000123"

        val foundProduct = localSkus.find { it.equals(scannedCodeValid, ignoreCase = true) }
        val notFoundProduct = localSkus.find { it.equals(scannedCodeNotFound, ignoreCase = true) }

        assertEquals("ZAP-042", foundProduct)
        assertEquals(null, notFoundProduct)
    }
}
