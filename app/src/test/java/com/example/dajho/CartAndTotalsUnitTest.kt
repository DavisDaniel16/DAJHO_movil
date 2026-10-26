package com.example.dajho

import com.example.dajho.catalog.Product
import com.example.dajho.catalog.ProductVariant
import com.example.dajho.pos.CartItem
import com.example.dajho.util.toCentsLong
import com.example.dajho.util.toCurrencyString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para Carrito de Compras, Subtotales y Totales en Céntimos (Fase 17).
 */
class CartAndTotalsUnitTest {

    @Test
    fun cartSubtotalAndTotalCalculationTest() {
        val prod1 = Product(
            sku = "CAM-001", name = "Camiseta Polo", brand = "DAJHO", category = "Ropa",
            cost = 6.50, salePrice = 12.50
        )
        val var1 = ProductVariant(size = "M", color = "Negro", stock = 10)

        val prod2 = Product(
            sku = "ZAP-042", name = "Zapatilla Urbana", brand = "DAJHO", category = "Calzado",
            cost = 14.00, salePrice = 25.00
        )
        val var2 = ProductVariant(size = "40", color = "Blanco", stock = 8)

        val cartItem1 = CartItem(product = prod1, variant = var1, quantity = 2)
        val cartItem2 = CartItem(product = prod2, variant = var2, quantity = 1)

        val subtotal1Cents = cartItem1.subtotal().toCentsLong()
        val subtotal2Cents = cartItem2.subtotal().toCentsLong()
        val totalCents = subtotal1Cents + subtotal2Cents

        assertEquals(2500L, subtotal1Cents) // $25.00
        assertEquals(2500L, subtotal2Cents) // $25.00
        assertEquals(5000L, totalCents)      // $50.00
        assertEquals("$50.00", totalCents.toCurrencyString())
    }

    @Test
    fun cartQuantityIncrementLimitTest() {
        val maxStock = 5
        var currentQty = 4

        val canIncrement = (currentQty + 1) <= maxStock
        if (canIncrement) currentQty++

        assertEquals(5, currentQty)

        val canIncrementFurther = (currentQty + 1) <= maxStock
        assertFalse(canIncrementFurther)
    }
}
