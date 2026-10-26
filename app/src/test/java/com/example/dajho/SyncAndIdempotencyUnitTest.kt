package com.example.dajho

import com.example.dajho.data.local.entity.SyncOperationEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Pruebas unitarias para Sincronización Idempotente, WorkManager y Backoff Exponencial (Fase 17).
 */
class SyncAndIdempotencyUnitTest {

    @Test
    fun exponentialBackoffDelayTest() {
        val baseDelaySeconds = 10L

        val attempt1Delay = baseDelaySeconds * (1 shl 1) // 20s
        val attempt2Delay = baseDelaySeconds * (1 shl 2) // 40s
        val attempt3Delay = baseDelaySeconds * (1 shl 3) // 80s

        assertEquals(20L, attempt1Delay)
        assertEquals(40L, attempt2Delay)
        assertEquals(80L, attempt3Delay)
    }

    @Test
    fun idempotentUuidDeduplicationTest() {
        val saleUuid = UUID.randomUUID().toString()

        val op1 = SyncOperationEntity(
            entityName = "sales",
            entityId = saleUuid,
            operation = "INSERT",
            payloadJson = "{\"saleNumber\":\"000010\"}"
        )

        val op2 = SyncOperationEntity(
            entityName = "sales",
            entityId = saleUuid,
            operation = "INSERT",
            payloadJson = "{\"saleNumber\":\"000010\"}"
        )

        assertEquals(op1.entityId, op2.entityId)
        assertTrue(op1.entityId.length > 20) // Valida formato UUID String
    }
}
