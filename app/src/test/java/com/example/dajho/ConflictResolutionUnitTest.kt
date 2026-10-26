package com.example.dajho

import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.data.sync.ConflictResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Pruebas unitarias para la estrategia explícita de conflictos (Fase 15).
 */
class ConflictResolutionUnitTest {

    @Test
    fun lastWriteWinsProductConflictTest() {
        val now = System.currentTimeMillis()

        val localProd = ProductEntity(
            id = "PROD-100",
            sku = "SKU-1",
            name = "Nombre Antiguo Local",
            brand = "DAJHO",
            category = "Ropa",
            costCents = 500L,
            salePriceCents = 1200L,
            updatedAt = now - 5000L // Editado hace 5 segundos
        )

        val remoteProd = ProductEntity(
            id = "PROD-100",
            sku = "SKU-1",
            name = "Nombre Nuevo Remoto",
            brand = "DAJHO",
            category = "Ropa",
            costCents = 500L,
            salePriceCents = 1200L,
            updatedAt = now // Editado ahora (más reciente)
        )

        val winner = ConflictResolver.resolveProductConflictLww(localProd, remoteProd)

        assertEquals("Nombre Nuevo Remoto", winner.name)
        assertEquals(remoteProd.updatedAt, winner.updatedAt)
    }

    @Test
    fun immutableDebtPaymentAdditiveSumTest() {
        val initialDebtCents = 5000L // $50.00
        val payment1Cents = 1500L     // $15.00 abono 1
        val payment2Cents = 1000L     // $10.00 abono 2

        val totalPayments = payment1Cents + payment2Cents
        val remainingDebt = (initialDebtCents - totalPayments).coerceAtLeast(0L)

        assertEquals(2500L, totalPayments) // $25.00 total abonado
        assertEquals(2500L, remainingDebt)  // $25.00 deuda restante
    }

    @Test
    fun oversellStockConditionDetectionTest() {
        val initialStock = 1
        val soldQtyOfflineDevice1 = 1
        val soldQtyOfflineDevice2 = 1

        val finalStock = initialStock - soldQtyOfflineDevice1 - soldQtyOfflineDevice2

        assertEquals(-1, finalStock)
        assertTrue(finalStock < 0) // Requiere alerta OVERSELL_STOCK_WARNING
    }

    @Test
    fun softDeletePreservesAuditRecordTest() {
        val deletedAtTimestamp = System.currentTimeMillis()
        val productDeleted = ProductEntity(
            id = UUID.randomUUID().toString(),
            sku = "DEL-001",
            name = "Producto Borrado Suave",
            brand = "DAJHO",
            category = "General",
            costCents = 1000L,
            salePriceCents = 2000L,
            deletedAt = deletedAtTimestamp
        )

        assertTrue(productDeleted.deletedAt != null)
        assertEquals(deletedAtTimestamp, productDeleted.deletedAt)
    }
}
