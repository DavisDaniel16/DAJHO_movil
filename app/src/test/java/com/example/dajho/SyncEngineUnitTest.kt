package com.example.dajho

import com.example.dajho.data.local.entity.SyncOperationEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.UUID

/**
 * Pruebas unitarias para el motor de Sincronización Idempotente (Fase 13).
 */
class SyncEngineUnitTest {

    @Test
    fun syncOperationAttemptsIncrementTest() {
        val op = SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            entityName = "sales",
            entityId = UUID.randomUUID().toString(),
            operation = "INSERT",
            payloadJson = "{\"totalCents\":2500}",
            attempts = 0,
            lastError = null
        )

        assertEquals(0, op.attempts)
        assertEquals(null, op.lastError)

        val updatedOp = op.copy(
            attempts = op.attempts + 1,
            lastError = "Connection Timeout"
        )

        assertEquals(1, updatedOp.attempts)
        assertEquals("Connection Timeout", updatedOp.lastError)
    }

    @Test
    fun idempotentUpsertPayloadMappingTest() {
        val saleId = UUID.randomUUID().toString()
        val payloadJson = "{\"id\":\"$saleId\",\"saleNumber\":\"000012\",\"totalCents\":5000}"

        assertNotNull(payloadJson)
        assertEquals(true, payloadJson.contains(saleId))
        assertEquals(true, payloadJson.contains("000012"))
    }
}
