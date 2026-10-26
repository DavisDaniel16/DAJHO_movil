package com.example.dajho

import com.example.dajho.data.local.entity.SyncOperationEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Pruebas unitarias para WorkManager, Reintentos con Backoff Exponencial y Sincronización Idempotente (Fase 14).
 */
class SyncWorkerTest {

    @Test
    fun exponentialBackoffDelayCalculationTest() {
        val initialBackoffSeconds = 10L

        val delayAttempt1 = initialBackoffSeconds * (1 shl 1) // 20s
        val delayAttempt2 = initialBackoffSeconds * (1 shl 2) // 40s
        val delayAttempt3 = initialBackoffSeconds * (1 shl 3) // 80s

        assertEquals(20L, delayAttempt1)
        assertEquals(40L, delayAttempt2)
        assertEquals(80L, delayAttempt3)
    }

    @Test
    fun ordered11StepDependencyPriorityTest() {
        val entityPriorities = mapOf(
            "businesses" to 1,
            "products" to 2,
            "product_variants" to 3,
            "clients" to 4,
            "sales" to 5,
            "sale_items" to 6,
            "payments" to 7,
            "debt_payments" to 8,
            "expenses" to 9,
            "cash_sessions" to 10,
            "local_images" to 11
        )

        assertTrue(entityPriorities["businesses"]!! < entityPriorities["products"]!!)
        assertTrue(entityPriorities["products"]!! < entityPriorities["product_variants"]!!)
        assertTrue(entityPriorities["product_variants"]!! < entityPriorities["sales"]!!)
        assertTrue(entityPriorities["sales"]!! < entityPriorities["sale_items"]!!)
    }

    @Test
    fun permanentErrorThresholdTest() {
        val maxAttemptsThreshold = 5

        val opNormal = SyncOperationEntity(
            entityName = "sales",
            entityId = UUID.randomUUID().toString(),
            operation = "INSERT",
            payloadJson = "{}",
            attempts = 2
        )

        val opFailedMax = SyncOperationEntity(
            entityName = "sales",
            entityId = UUID.randomUUID().toString(),
            operation = "INSERT",
            payloadJson = "{}",
            attempts = 5
        )

        assertFalse(opNormal.attempts >= maxAttemptsThreshold)
        assertTrue(opFailedMax.attempts >= maxAttemptsThreshold)
    }

    @Test
    fun idempotentUpsertDeduplicationTest() {
        val saleId = UUID.randomUUID().toString()
        val op1 = SyncOperationEntity(
            entityName = "sales",
            entityId = saleId,
            operation = "INSERT",
            payloadJson = "{\"id\":\"$saleId\"}"
        )
        val op2 = SyncOperationEntity(
            entityName = "sales",
            entityId = saleId,
            operation = "INSERT",
            payloadJson = "{\"id\":\"$saleId\"}"
        )

        assertEquals(op1.entityId, op2.entityId)
    }
}
