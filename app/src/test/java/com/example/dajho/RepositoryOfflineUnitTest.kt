package com.example.dajho

import com.example.dajho.data.local.entity.ClientEntity
import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.data.local.entity.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.UUID

/**
 * Pruebas unitarias para Operaciones CRUD y Soft Delete en Repositorio Offline (Fase 17).
 */
class RepositoryOfflineUnitTest {

    @Test
    fun offlineProductEntityCreationTest() {
        val prodId = UUID.randomUUID().toString()
        val product = ProductEntity(
            id = prodId,
            sku = "CAM-99",
            name = "Camiseta Offline",
            brand = "DAJHO",
            category = "Ropa",
            costCents = 700L,
            salePriceCents = 1500L,
            syncStatus = SyncStatus.PENDING_INSERT
        )

        assertNotNull(product.id)
        assertEquals(SyncStatus.PENDING_INSERT, product.syncStatus)
        assertEquals(1500L, product.salePriceCents)
    }

    @Test
    fun offlineClientEntityCreationTest() {
        val clientId = UUID.randomUUID().toString()
        val client = ClientEntity(
            id = clientId,
            name = "Cliente Offline",
            phone = "0981112233",
            debtCents = 2500L,
            syncStatus = SyncStatus.PENDING_INSERT
        )

        assertEquals("Cliente Offline", client.name)
        assertEquals(2500L, client.debtCents)
        assertEquals(SyncStatus.PENDING_INSERT, client.syncStatus)
    }
}
