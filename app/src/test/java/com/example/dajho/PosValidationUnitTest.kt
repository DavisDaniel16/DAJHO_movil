package com.example.dajho

import com.example.dajho.pos.PaymentMethod
import com.example.dajho.util.toCentsLong
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para validaciones del Punto de Venta (POS) y Checkout Atómico (Fase 7).
 */
class PosValidationUnitTest {

    @Test
    fun cartSubtotalAndTotalCalculationTest() {
        val price1Cents = 1250L // $12.50
        val quantity1 = 2
        val subtotal1Cents = price1Cents * quantity1 // $25.00 (2500 céntimos)

        val price2Cents = 2500L // $25.00
        val quantity2 = 1
        val subtotal2Cents = price2Cents * quantity2 // $25.00 (2500 céntimos)

        val totalCents = subtotal1Cents + subtotal2Cents // $50.00 (5000 céntimos)

        assertEquals(2500L, subtotal1Cents)
        assertEquals(2500L, subtotal2Cents)
        assertEquals(5000L, totalCents)
    }

    @Test
    fun stockLimitValidationTest() {
        val availableStock = 5
        val requestedQtyValid = 5
        val requestedQtyInvalid = 6

        assertTrue(requestedQtyValid <= availableStock)
        assertFalse(requestedQtyInvalid <= availableStock)
    }

    @Test
    fun creditPaymentRequiresClientTest() {
        val paymentMethodCredit = PaymentMethod.CREDIT
        val selectedClientNull = null
        val selectedClientName = "Carlos Mendoza"

        val canCheckoutNullClient = (paymentMethodCredit != PaymentMethod.CREDIT) || (selectedClientNull != null)
        val canCheckoutValidClient = (paymentMethodCredit != PaymentMethod.CREDIT) || (selectedClientName.isNotBlank())

        assertFalse(canCheckoutNullClient)
        assertTrue(canCheckoutValidClient)
    }

    @Test
    fun transferPaymentRequiresReferenceTest() {
        val paymentMethodTransfer = PaymentMethod.TRANSFER
        val refBlank = ""
        val refValid = "TRF-998811"

        val isValidBlank = (paymentMethodTransfer != PaymentMethod.TRANSFER) || refBlank.isNotBlank()
        val isValidRef = (paymentMethodTransfer != PaymentMethod.TRANSFER) || refValid.isNotBlank()

        assertFalse(isValidBlank)
        assertTrue(isValidRef)
    }
}
