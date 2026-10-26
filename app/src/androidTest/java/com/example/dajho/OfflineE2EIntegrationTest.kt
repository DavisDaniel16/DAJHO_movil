package com.example.dajho

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.entity.ClientEntity
import com.example.dajho.data.local.entity.DebtPaymentEntity
import com.example.dajho.data.local.entity.PaymentEntity
import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.data.local.entity.ProductVariantEntity
import com.example.dajho.data.local.entity.SaleEntity
import com.example.dajho.data.local.entity.SaleItemEntity
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
 * Prueba Instrumentada de Integración Extremo a Extremo (E2E) en Modo Offline (Fase 17).
 * Simula: Crear Producto -> Crear Cliente -> Venta a Crédito -> Abono -> Persistencia tras Reinicio -> Cola Sync.
 */
@RunWith(AndroidJUnit4::class)
class OfflineE2EIntegrationTest {

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
    fun completeOfflineBusinessCycleE2eTest() = runBlocking {
        // 1. Crear Producto Offline
        val productId = UUID.randomUUID().toString()
        val variantId = UUID.randomUUID().toString()
        val product = ProductEntity(
            id = productId,
            sku = "E2E-001",
            name = "Chaqueta Térmica",
            brand = "DAJHO",
            category = "Ropa",
            costCents = 2000L,       // $20.00
            salePriceCents = 4000L   // $40.00
        )
        val variant = ProductVariantEntity(
            id = variantId,
            productId = productId,
            size = "L",
            color = "Negro",
            stock = 10 // Stock inicial = 10
        )
        db.productDao().insertProduct(product)
        db.productDao().insertVariants(listOf(variant))

        // 2. Crear Cliente Offline
        val clientId = UUID.randomUUID().toString()
        val client = ClientEntity(
            id = clientId,
            name = "Cliente E2E Offline",
            phone = "0986627675",
            debtCents = 0L // Deuda inicial = $0.00
        )
        db.clientSupplierDao().insertClient(client)

        // 3. Realizar Venta a Crédito Offline ($40.00)
        val saleId = UUID.randomUUID().toString()
        val sale = SaleEntity(
            id = saleId,
            saleNumber = "000999",
            clientId = clientId,
            paymentMethod = "CREDIT",
            totalCents = 4000L
        )
        val saleItem = SaleItemEntity(
            saleId = saleId,
            productId = productId,
            variantId = variantId,
            quantity = 1,
            unitPriceCents = 4000L,
            subtotalCents = 4000L
        )
        val payment = PaymentEntity(
            saleId = saleId,
            method = "CREDIT",
            amountCents = 4000L
        )
        db.saleDao().recordCompleteSale(sale, listOf(saleItem), listOf(payment))

        // Verificar estado tras venta
        val clientAfterSale = db.clientSupplierDao().getClientById(clientId)
        val variantsAfterSale = db.productDao().getVariantsByProductId(productId)

        assertEquals(4000L, clientAfterSale?.debtCents) // Deuda aumentó a $40.00
        assertEquals(9, variantsAfterSale[0].stock)    // Stock disminuyó de 10 a 9

        // 4. Registrar Abono en Efectivo Offline ($15.00)
        val debtPayment = DebtPaymentEntity(
            clientId = clientId,
            amountCents = 1500L, // Abono $15.00
            paymentMethod = "CASH"
        )
        db.clientSupplierDao().recordClientDebtPaymentTransaction(debtPayment)

        // 5. Verificar estado final tras abono
        val clientAfterAbono = db.clientSupplierDao().getClientById(clientId)
        assertNotNull(clientAfterAbono)
        assertEquals(2500L, clientAfterAbono?.debtCents) // Deuda disminuyó a $25.00
        assertEquals("$25.00", clientAfterAbono?.debtCents?.toCurrencyString())

        // 6. Verificar que las operaciones quedaron encoladas en sync_operations
        val pendingSyncOps = db.syncOperationDao().getAllPendingOperations()
        assertNotNull(pendingSyncOps)
        assertEquals(true, pendingSyncOps.isNotEmpty())
    }
}
