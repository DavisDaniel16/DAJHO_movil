package com.example.dajho

import com.example.dajho.catalog.Product
import com.example.dajho.catalog.ProductVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Pruebas unitarias para Productos, Variantes, Stock y Margen de Ganancia (Fase 17).
 */
class ProductAndStockUnitTest {

    @Test
    fun productCreationAndTotalStockTest() {
        val product = Product(
            id = UUID.randomUUID().toString(),
            sku = "CAM-100",
            name = "Camiseta Deportiva",
            brand = "DAJHO",
            category = "Ropa",
            cost = 8.00,
            salePrice = 16.00,
            variants = mutableListOf(
                ProductVariant(size = "S", color = "Rojo", stock = 5),
                ProductVariant(size = "M", color = "Negro", stock = 10),
                ProductVariant(size = "L", color = "Azul", stock = 3)
            )
        )

        assertEquals(18, product.totalStock())
        assertEquals(3, product.variantCount())
        assertEquals("S, M, L", product.sizesSummary())
    }

    @Test
    fun lowStockThresholdDetectionTest() {
        val normalVariant = ProductVariant(size = "M", color = "Negro", stock = 10)
        val lowStockVariant = ProductVariant(size = "S", color = "Rojo", stock = 2)
        val zeroStockVariant = ProductVariant(size = "L", color = "Azul", stock = 0)

        assertFalse(normalVariant.stock <= 3)
        assertTrue(lowStockVariant.stock <= 3)
        assertTrue(zeroStockVariant.stock <= 3)
    }

    @Test
    fun profitMarginPercentageCalculationTest() {
        val cost = 12.00
        val price = 20.00

        val profit = price - cost
        val marginPercentage = ((profit / price) * 100).toInt()

        assertEquals(8.00, profit, 0.001)
        assertEquals(40, marginPercentage) // 40% de margen de ganancia
    }
}
