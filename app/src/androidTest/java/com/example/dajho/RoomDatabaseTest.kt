package com.example.dajho

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.entity.ClientEntity
import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.util.toCurrencyString
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * Prueba de integración de la Base de Datos Room (Fase 2).
 */
@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetProductTest() = runBlocking {
        val productId = UUID.randomUUID().toString()
        val product = ProductEntity(
            id = productId,
            sku = "TEST-100",
            name = "Camiseta de Prueba",
            brand = "DAJHO",
            category = "Ropa",
            costCents = 500L,       // $5.00
            salePriceCents = 1250L  // $12.50
        )

        db.productDao().insertProduct(product)

        val retrieved = db.productDao().getProductById(productId)

        assertNotNull(retrieved)
        assertEquals("TEST-100", retrieved?.sku)
        assertEquals(1250L, retrieved?.salePriceCents)
        assertEquals("$12.50", retrieved?.salePriceCents?.toCurrencyString())
    }

    @Test
    fun insertAndGetClientTest() = runBlocking {
        val clientId = UUID.randomUUID().toString()
        val client = ClientEntity(
            id = clientId,
            name = "Danny Vacacela",
            phone = "0986627675",
            debtCents = 2850L // $28.50
        )

        db.clientDao().insertClient(client)

        val retrieved = db.clientDao().getClientById(clientId)

        assertNotNull(retrieved)
        assertEquals("Danny Vacacela", retrieved?.name)
        assertEquals(2850L, retrieved?.debtCents)
        assertEquals("$28.50", retrieved?.debtCents?.toCurrencyString())
    }
}
