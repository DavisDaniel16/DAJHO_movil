package com.example.dajho

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.entity.ClientEntity
import com.example.dajho.data.local.entity.ExpenseEntity
import com.example.dajho.data.local.entity.PaymentEntity
import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.data.local.entity.ProductVariantEntity
import com.example.dajho.data.local.entity.SaleEntity
import com.example.dajho.data.local.entity.SaleItemEntity
import com.example.dajho.data.local.entity.SupplierEntity
import com.example.dajho.data.local.entity.SyncOperationEntity
import com.example.dajho.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * Pruebas unitarias completas de DAOs y Transacciones Atómicas en Room (Fase 3).
 */
@RunWith(AndroidJUnit4::class)
class RoomDaoTest {

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
    fun atomicSaleTransactionTest() = runBlocking {
        // 1. Crear Cliente
        val clientId = UUID.randomUUID().toString()
        val client = ClientEntity(
            id = clientId,
            name = "Carlos Mendoza",
            phone = "0987654321",
            debtCents = 0L // Inicialmente sin deuda
        )
        db.clientSupplierDao().insertClient(client)

        // 2. Crear Producto y Variante
        val productId = UUID.randomUUID().toString()
        val variantId = UUID.randomUUID().toString()
        val product = ProductEntity(
            id = productId,
            sku = "ZAP-001",
            name = "Zapatilla Urbana",
            brand = "DAJHO",
            category = "Calzado",
            costCents = 1000L,
            salePriceCents = 2500L // $25.00
        )
        val variant = ProductVariantEntity(
            id = variantId,
            productId = productId,
            size = "40",
            color = "Negro",
            stock = 10 // Stock inicial: 10
        )
        db.productDao().insertProduct(product)
        db.productDao().insertVariants(listOf(variant))

        // 3. Crear Registro de Venta Atómica a Crédito ($25.00 / 2500 céntimos)
        val saleId = UUID.randomUUID().toString()
        val sale = SaleEntity(
            id = saleId,
            saleNumber = "000001",
            clientId = clientId,
            paymentMethod = "CREDIT",
            totalCents = 2500L
        )
        val saleItem = SaleItemEntity(
            saleId = saleId,
            productId = productId,
            variantId = variantId,
            quantity = 2,
            unitPriceCents = 2500L,
            subtotalCents = 5000L
        )
        val payment = PaymentEntity(
            saleId = saleId,
            method = "CREDIT",
            amountCents = 2500L
        )

        // Ejecutar Transacción Atómica
        db.saleDao().recordCompleteSale(sale, listOf(saleItem), listOf(payment))

        // 4. Verificar Resultados en SQLite
        val savedSale = db.saleDao().getSaleById(saleId)
        assertNotNull(savedSale)
        assertEquals("000001", savedSale?.saleNumber)

        // Verificar que la deuda del cliente aumentó a $25.00 (2500 céntimos)
        val updatedClient = db.clientSupplierDao().getClientById(clientId)
        assertEquals(2500L, updatedClient?.debtCents)

        // Verificar que el stock de la variante disminuyó de 10 a 8
        val updatedVariants = db.productDao().getVariantsByProductId(productId)
        assertEquals(1, updatedVariants.size)
        assertEquals(8, updatedVariants[0].stock)
    }

    @Test
    fun supplierAndExpenseDaoTest() = runBlocking {
        val supplier = SupplierEntity(
            name = "Textiles Ambato",
            phone = "0991112233",
            debtCents = 12000L // $120.00
        )
        db.clientSupplierDao().insertSupplier(supplier)

        val suppliers = db.clientSupplierDao().getAllSuppliersFlow("DEFAULT_BUSINESS").first()
        assertEquals(1, suppliers.size)
        assertEquals("Textiles Ambato", suppliers[0].name)

        val expense = ExpenseEntity(
            category = "Mercadería",
            concept = "Compra de Hilos",
            amountCents = 3500L // $35.00
        )
        db.expenseDao().insertExpense(expense)

        val expenses = db.expenseDao().getAllExpensesFlow("DEFAULT_BUSINESS").first()
        assertEquals(1, expenses.size)
        assertEquals(3500L, expenses[0].amountCents)
    }

    @Test
    fun syncOperationQueueTest() = runBlocking {
        val syncOp = SyncOperationEntity(
            entityName = "products",
            entityId = UUID.randomUUID().toString(),
            operation = "INSERT",
            payloadJson = "{\"sku\":\"CAM-001\"}"
        )
        db.syncOperationDao().insertOperation(syncOp)

        val pending = db.syncOperationDao().getAllPendingOperations()
        assertEquals(1, pending.size)
        assertEquals("products", pending[0].entityName)
    }
}
