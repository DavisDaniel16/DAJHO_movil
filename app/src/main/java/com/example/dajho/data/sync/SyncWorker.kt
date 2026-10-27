package com.example.dajho.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.dajho.BuildConfig
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.datastore.UserPreferences
import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.data.local.entity.ProductVariantEntity
import com.example.dajho.data.local.entity.ClientEntity
import com.example.dajho.data.local.entity.SupplierEntity
import com.example.dajho.data.local.entity.SaleEntity
import com.example.dajho.data.local.entity.SaleItemEntity
import com.example.dajho.data.local.entity.PaymentEntity
import com.example.dajho.data.local.entity.DebtPaymentEntity
import com.example.dajho.data.local.entity.ExpenseEntity
import com.example.dajho.data.local.entity.CashSessionEntity
import com.example.dajho.data.local.entity.CashMovementEntity
<<<<<<< HEAD
import com.example.dajho.data.local.entity.SyncOperationEntity
import com.example.dajho.data.local.dao.SyncOperationDao
=======
>>>>>>> 0f93a4a83ec40e8ea28347f46453a5399c98f3e6
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.io.File
import org.json.JSONArray
<<<<<<< HEAD
import org.json.JSONObject
=======
>>>>>>> 0f93a4a83ec40e8ea28347f46453a5399c98f3e6
import kotlinx.coroutines.flow.first

/**
 * Worker de WorkManager para Sincronización Automática e Idempotente con Supabase (Fase 14).
 * Incluye escaneo automático de productos huérfanos/pendientes de sincronización.
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "DajhoOfflineSyncWorker"
        const val MAX_ATTEMPTS = 5
    }

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val syncDao = db.syncOperationDao()
        val imageDao = db.localImageDao()
        val accessToken = UserPreferences(applicationContext).accessTokenFlow.first()
            ?: return Result.failure()

        val httpClient = HttpClient(Android)
        return try {
<<<<<<< HEAD
            // 1. ESCANEAR Y ENCOLAR AUTOMÁTICAMENTE PRODUCTOS, CLIENTES Y VENTAS HUÉRFANOS O PENDIENTES
            scanAndQueueOrphanProducts(db, syncDao)
            scanAndQueueOrphanEntities(db, syncDao)
=======
            val pendingOps = syncDao.getAllPendingOperations()
            var retryRequired = false
>>>>>>> 0f93a4a83ec40e8ea28347f46453a5399c98f3e6

            val pendingOps = syncDao.getAllPendingOperations()
            var retryRequired = false

            // Ordenar operaciones según las etapas de dependencia relacional
            val orderedOps = pendingOps.sortedBy { getEntityPriority(it.entityName) }

            for (op in orderedOps) {
                if (op.attempts >= MAX_ATTEMPTS) {
                    syncDao.recordErrorAttempt(op.id, "PERMANENT_ERROR: Límite de reintentos alcanzado")
                    continue
                }

                try {
                    executeIdempotentPush(httpClient, accessToken, op)
                    syncDao.deleteOperation(op.id)
                } catch (ex: Exception) {
                    retryRequired = true
                    syncDao.recordErrorAttempt(op.id, ex.message ?: "Error temporal de conexión")
                }
            }

            // Subir imágenes pendientes a Supabase Storage
            val pendingImages = imageDao.getPendingImages()
            for (img in pendingImages) {
                val file = File(img.localFilePath)
                if (file.exists() && file.length() > 0L) {
                    try {
                        uploadImage(httpClient, accessToken, img.remoteStoragePath, file)
                        imageDao.markSynced(img.id)
                    } catch (_: Exception) {
                        retryRequired = true
                        imageDao.recordUploadError(img.id)
                    }
                } else {
                    imageDao.recordUploadError(img.id)
                }
            }

            // PULL SYNC: Descargar cambios remotos actualizados en Supabase
            executePullSyncFromSupabase(httpClient, accessToken, db)

            if (retryRequired) Result.retry() else Result.success()
        } catch (_: Exception) {
            Result.retry()
        } finally {
            httpClient.close()
        }
    }

<<<<<<< HEAD
    /**
     * Detecta productos locales cuyo estado no sea SYNCED y los encola automáticamente
     * en la tabla de operaciones de sincronización utilizando verificación previa y OnConflict.IGNORE.
     */
    private suspend fun scanAndQueueOrphanProducts(db: AppDatabase, syncDao: SyncOperationDao) {
        val pendingProducts = db.productDao().getPendingSyncProducts()
        for (product in pendingProducts) {
            val variants = db.productDao().getVariantsByProductId(product.id)

            // 1. Verificar si ya existe una operación pendiente para este producto
            val existingProductOp = syncDao.getOperationByEntityId(product.id)
            if (existingProductOp == null) {
                syncDao.insertOperation(
                    SyncOperationEntity(
                        entityName = "products",
                        entityId = product.id,
                        operation = "INSERT",
                        payloadJson = JSONObject().apply {
                            put("id", product.id)
                            put("negocio_id", product.businessId)
                            put("sku", product.sku)
                            put("nombre", product.name)
                            put("marca", product.brand)
                            put("categoria", product.category)
                            put("costo_centavos", product.costCents)
                            put("precio_venta_centavos", product.salePriceCents)
                            put("uri_imagen", product.imageUri ?: JSONObject.NULL)
                        }.toString()
                    )
                )
            }

            // 2. Verificar e inyectar operaciones para cada variante pendiente
            for (v in variants) {
                val existingVariantOp = syncDao.getOperationByEntityId(v.id)
                if (existingVariantOp == null) {
                    syncDao.insertOperation(
                        SyncOperationEntity(
                            entityName = "product_variants",
                            entityId = v.id,
                            operation = "INSERT",
                            payloadJson = JSONObject().apply {
                                put("id", v.id)
                                put("producto_id", v.productId)
                                put("talla", v.size)
                                put("color", v.color)
                                put("stock", v.stock)
                            }.toString()
                        )
                    )
                }
            }
        }
    }

    /**
     * Escanea y encola automáticamente Clientes, Proveedores y Ventas locales huérfanos o pendientes de sincronización.
     */
    private suspend fun scanAndQueueOrphanEntities(db: AppDatabase, syncDao: SyncOperationDao) {
        // 1. Clientes huérfanos/pendientes
        val pendingClients = db.clientSupplierDao().getPendingSyncClients()
        for (client in pendingClients) {
            val existingOp = syncDao.getOperationByEntityId(client.id)
            if (existingOp == null) {
                syncDao.insertOperation(
                    SyncOperationEntity(
                        entityName = "clients",
                        entityId = client.id,
                        operation = "INSERT",
                        payloadJson = JSONObject().apply {
                            put("id", client.id)
                            put("negocio_id", client.businessId)
                            put("nombre", client.name)
                            put("telefono", client.phone)
                            put("info", client.info)
                            put("deuda_centavos", client.debtCents)
                        }.toString()
                    )
                )
            }
        }

        // 2. Proveedores huérfanos/pendientes
        val pendingSuppliers = db.clientSupplierDao().getPendingSyncSuppliers()
        for (supplier in pendingSuppliers) {
            val existingOp = syncDao.getOperationByEntityId(supplier.id)
            if (existingOp == null) {
                syncDao.insertOperation(
                    SyncOperationEntity(
                        entityName = "suppliers",
                        entityId = supplier.id,
                        operation = "INSERT",
                        payloadJson = JSONObject().apply {
                            put("id", supplier.id)
                            put("negocio_id", supplier.businessId)
                            put("nombre", supplier.name)
                            put("telefono", supplier.phone)
                            put("info", supplier.info)
                            put("deuda_centavos", supplier.debtCents)
                        }.toString()
                    )
                )
            }
        }

        // 3. Ventas pendientes
        val pendingSales = db.saleDao().getPendingSyncSales()
        for (sale in pendingSales) {
            val existingOp = syncDao.getOperationByEntityId(sale.id)
            if (existingOp == null) {
                syncDao.insertOperation(
                    SyncOperationEntity(
                        entityName = "sales",
                        entityId = sale.id,
                        operation = "INSERT",
                        payloadJson = JSONObject().apply {
                            put("id", sale.id)
                            put("negocio_id", sale.businessId)
                            put("usuario_id", sale.userId)
                            put("numero_venta", sale.saleNumber)
                            put("cliente_id", sale.clientId ?: JSONObject.NULL)
                            put("metodo_pago", sale.paymentMethod)
                            put("referencia_transferencia", sale.transferReference ?: JSONObject.NULL)
                            put("total_centavos", sale.totalCents)
                            put("marca_tiempo", sale.timestamp)
                        }.toString()
                    )
                )
            }
        }
    }

    private suspend fun executeIdempotentPush(
        httpClient: HttpClient,
        accessToken: String,
        op: SyncOperationEntity
    ) {
        val table = remoteTableName(op.entityName)
        val baseUrl = "${BuildConfig.SUPABASE_URL}/rest/v1/$table"
        val response = if (op.operation == "DELETE") {
            httpClient.delete("$baseUrl?id=eq.${op.entityId}") {
                addSupabaseHeaders(accessToken)
            }
        } else if (op.operation == "UPDATE") {
            httpClient.request("$baseUrl?id=eq.${op.entityId}") {
                method = HttpMethod.Patch
                addSupabaseHeaders(accessToken)
                header("Prefer", "return=minimal")
                contentType(ContentType.Application.Json)
                setBody(op.payloadJson)
            }
        } else {
            httpClient.post("$baseUrl?on_conflict=id") {
                addSupabaseHeaders(accessToken)
                header("Prefer", "resolution=merge-duplicates,return=minimal")
                contentType(ContentType.Application.Json)
                setBody(op.payloadJson)
            }
        }

        check(response.status.isSuccess()) {
            "Supabase rechazó ${op.operation} ${op.entityName}/${op.entityId}: HTTP ${response.status.value}"
        }
    }

=======
    private suspend fun executeIdempotentPush(
        httpClient: HttpClient,
        accessToken: String,
        op: com.example.dajho.data.local.entity.SyncOperationEntity
    ) {
        val table = remoteTableName(op.entityName)
        val baseUrl = "${BuildConfig.SUPABASE_URL}/rest/v1/$table"
        val response = if (op.operation == "DELETE") {
            httpClient.delete("$baseUrl?id=eq.${op.entityId}") {
                addSupabaseHeaders(accessToken)
            }
        } else if (op.operation == "UPDATE") {
            httpClient.request("$baseUrl?id=eq.${op.entityId}") {
                method = HttpMethod.Patch
                addSupabaseHeaders(accessToken)
                header("Prefer", "return=minimal")
                contentType(ContentType.Application.Json)
                setBody(op.payloadJson)
            }
        } else {
            httpClient.post("$baseUrl?on_conflict=id") {
                addSupabaseHeaders(accessToken)
                header("Prefer", "resolution=merge-duplicates,return=minimal")
                contentType(ContentType.Application.Json)
                setBody(op.payloadJson)
            }
        }

        check(response.status.isSuccess()) {
            "Supabase rechazó ${op.operation} ${op.entityName}/${op.entityId}: HTTP ${response.status.value}"
        }
    }

>>>>>>> 0f93a4a83ec40e8ea28347f46453a5399c98f3e6
    private suspend fun executePullSyncFromSupabase(httpClient: HttpClient, accessToken: String, db: AppDatabase) {
        pullProducts(httpClient, accessToken, db)
        pullClients(httpClient, accessToken, db)
        pullSales(httpClient, accessToken, db)
        pullPayments(httpClient, accessToken, db)
        pullDebtPayments(httpClient, accessToken, db)
        pullExpenses(httpClient, accessToken, db)
        pullCash(httpClient, accessToken, db)
    }

    private suspend fun pullProducts(httpClient: HttpClient, accessToken: String, db: AppDatabase) {
        val products = getRemoteRows(httpClient, accessToken, "productos")
        for (row in products) {
            val product = ProductEntity(
                id = row.getString("id"),
                businessId = row.optString("negocio_id", "DEFAULT_BUSINESS"),
                sku = row.getString("sku"),
                name = row.getString("nombre"),
                brand = row.optString("marca", "DAJHO"),
                category = row.optString("categoria", "General"),
                costCents = row.optLong("costo_centavos", 0L),
                salePriceCents = row.optLong("precio_venta_centavos", 0L),
                imageUri = row.optString("uri_imagen").takeUnless { it.isBlank() },
                syncStatus = com.example.dajho.data.local.entity.SyncStatus.SYNCED
            )
            db.productDao().insertProduct(product)
        }

        val variants = getRemoteRows(httpClient, accessToken, "variantes_producto")
        db.productDao().insertVariants(
            variants.map {
                ProductVariantEntity(
                    id = it.getString("id"),
                    productId = it.getString("producto_id"),
                    size = it.optString("talla", "Única"),
                    color = it.optString("color", "Único"),
                    stock = it.optInt("stock", 0),
                    syncStatus = com.example.dajho.data.local.entity.SyncStatus.SYNCED
                )
            }
        )
    }

    private suspend fun pullClients(httpClient: HttpClient, accessToken: String, db: AppDatabase) {
        val clients = getRemoteRows(httpClient, accessToken, "clientes")
        for (row in clients) {
            db.clientSupplierDao().insertClient(
                ClientEntity(
                    id = row.getString("id"),
                    businessId = row.optString("negocio_id", "DEFAULT_BUSINESS"),
                    name = row.getString("nombre"),
                    phone = row.optString("telefono", ""),
                    info = row.optString("info", ""),
                    debtCents = row.optLong("deuda_centavos", 0L),
                    syncStatus = com.example.dajho.data.local.entity.SyncStatus.SYNCED
                )
            )
        }
        for (row in getRemoteRows(httpClient, accessToken, "proveedores")) {
            db.clientSupplierDao().insertSupplier(
                SupplierEntity(
                    id = row.getString("id"),
                    businessId = row.optString("negocio_id", "DEFAULT_BUSINESS"),
                    name = row.getString("nombre"),
                    phone = row.optString("telefono", ""),
                    info = row.optString("info", ""),
                    debtCents = row.optLong("deuda_centavos", 0L),
                    syncStatus = com.example.dajho.data.local.entity.SyncStatus.SYNCED
                )
            )
        }
    }

    private suspend fun pullSales(httpClient: HttpClient, accessToken: String, db: AppDatabase) {
        for (row in getRemoteRows(httpClient, accessToken, "ventas")) {
            db.saleDao().insertSale(
                SaleEntity(
                    id = row.getString("id"),
                    businessId = row.optString("negocio_id", "DEFAULT_BUSINESS"),
                    userId = row.optString("usuario_id", "DEFAULT_USER"),
                    saleNumber = row.getString("numero_venta"),
                    clientId = row.optString("cliente_id").takeUnless { it.isBlank() },
                    paymentMethod = row.getString("metodo_pago"),
                    transferReference = row.optString("referencia_transferencia").takeUnless { it.isBlank() },
                    totalCents = row.optLong("total_centavos", 0L),
                    timestamp = row.optLong("marca_tiempo", 0L),
                    syncStatus = com.example.dajho.data.local.entity.SyncStatus.SYNCED
                )
            )
        }
        db.saleDao().insertSaleItems(
            getRemoteRows(httpClient, accessToken, "detalles_venta").map {
                SaleItemEntity(
                    id = it.getString("id"),
                    saleId = it.getString("venta_id"),
                    productId = it.getString("producto_id"),
                    variantId = it.getString("variante_id"),
                    quantity = it.optInt("cantidad", 0),
                    unitPriceCents = it.optLong("precio_unitario_centavos", 0L),
                    subtotalCents = it.optLong("subtotal_centavos", 0L)
                )
            }
        )
    }

    private suspend fun pullPayments(httpClient: HttpClient, accessToken: String, db: AppDatabase) {
        db.saleDao().insertPayments(
            getRemoteRows(httpClient, accessToken, "pagos").map {
                PaymentEntity(
                    id = it.getString("id"),
                    saleId = it.getString("venta_id"),
                    method = it.getString("metodo"),
                    amountCents = it.optLong("monto_centavos", 0L),
                    reference = it.optString("referencia").takeUnless { value -> value.isBlank() }
                )
            }
        )
    }

    private suspend fun pullDebtPayments(httpClient: HttpClient, accessToken: String, db: AppDatabase) {
        for (row in getRemoteRows(httpClient, accessToken, "abonos_deuda")) {
            db.clientSupplierDao().insertDebtPayment(
                DebtPaymentEntity(
                    id = row.getString("id"),
                    businessId = row.optString("negocio_id", "DEFAULT_BUSINESS"),
                    clientId = row.optString("cliente_id").takeUnless { it.isBlank() },
                    supplierId = row.optString("proveedor_id").takeUnless { it.isBlank() },
                    amountCents = row.optLong("monto_centavos", 0L),
                    paymentMethod = row.optString("metodo_pago", "CASH"),
                    timestamp = row.optLong("marca_tiempo", 0L),
                    syncStatus = com.example.dajho.data.local.entity.SyncStatus.SYNCED
                )
            )
        }
    }

    private suspend fun pullExpenses(httpClient: HttpClient, accessToken: String, db: AppDatabase) {
        for (row in getRemoteRows(httpClient, accessToken, "gastos")) {
            db.expenseDao().insertExpense(
                ExpenseEntity(
                    id = row.getString("id"),
                    businessId = row.optString("negocio_id", "DEFAULT_BUSINESS"),
                    category = row.optString("categoria", "Gasto Operativo"),
                    concept = row.getString("concepto"),
                    amountCents = row.optLong("monto_centavos", 0L),
                    timestamp = row.optLong("marca_tiempo", 0L),
                    syncStatus = com.example.dajho.data.local.entity.SyncStatus.SYNCED
                )
            )
        }
    }

    private suspend fun pullCash(httpClient: HttpClient, accessToken: String, db: AppDatabase) {
        for (row in getRemoteRows(httpClient, accessToken, "sesiones_caja")) {
            db.cashDao().insertShift(
                CashSessionEntity(
                    id = row.getString("id"),
                    businessId = row.optString("negocio_id", "DEFAULT_BUSINESS"),
                    userId = row.optString("usuario_id", "DEFAULT_USER"),
                    initialCashCents = row.optLong("efectivo_inicial_centavos", 0L),
                    expectedCashCents = row.optLong("efectivo_esperado_centavos", 0L),
                    actualCashCents = if (row.isNull("efectivo_contado_centavos")) null else row.optLong("efectivo_contado_centavos"),
                    isOpen = row.optBoolean("esta_abierta", true),
                    openedAt = row.optLong("abierta_en", 0L),
                    closedAt = if (row.isNull("cerrada_en")) null else row.optLong("cerrada_en"),
                    syncStatus = com.example.dajho.data.local.entity.SyncStatus.SYNCED
                )
            )
        }
        for (row in getRemoteRows(httpClient, accessToken, "movimientos_caja")) {
            db.cashDao().insertTransaction(
                CashMovementEntity(
                    id = row.getString("id"),
                    sessionId = row.getString("sesion_id"),
                    concept = row.getString("concepto"),
                    amountCents = row.optLong("monto_centavos", 0L),
                    movementType = row.getString("tipo_movimiento"),
                    relatedSaleId = row.optString("venta_relacionada_id").takeUnless { it.isBlank() },
                    relatedClientId = row.optString("cliente_relacionado_id").takeUnless { it.isBlank() },
                    timestamp = row.optLong("marca_tiempo", 0L)
                )
            )
        }
    }

<<<<<<< HEAD
    private suspend fun getRemoteRows(httpClient: HttpClient, accessToken: String, table: String): List<JSONObject> {
=======
    private suspend fun getRemoteRows(httpClient: HttpClient, accessToken: String, table: String): List<org.json.JSONObject> {
>>>>>>> 0f93a4a83ec40e8ea28347f46453a5399c98f3e6
        val response = httpClient.get("${BuildConfig.SUPABASE_URL}/rest/v1/$table?select=*") {
            addSupabaseHeaders(accessToken)
        }
        check(response.status.isSuccess()) {
            "Supabase rechazó la descarga de $table: HTTP ${response.status.value}"
        }
        return JSONArray(response.bodyAsText()).let { array ->
            (0 until array.length()).map { array.getJSONObject(it) }
        }
    }

    private suspend fun uploadImage(
        httpClient: HttpClient,
        accessToken: String,
        remotePath: String,
        file: File
    ) {
        val response = httpClient.put(
            "${BuildConfig.SUPABASE_URL}/storage/v1/object/product-images/$remotePath"
        ) {
            addSupabaseHeaders(accessToken)
            header("x-upsert", "true")
            contentType(ContentType.Application.OctetStream)
            setBody(file.readBytes())
        }
        check(response.status.isSuccess()) {
            "Supabase Storage rechazó la imagen: HTTP ${response.status.value}"
        }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.addSupabaseHeaders(accessToken: String) {
        header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        header(HttpHeaders.Authorization, "Bearer $accessToken")
    }

    private fun io.ktor.client.request.HttpRequestBuilder.addSupabaseHeaders() {
        header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        header(HttpHeaders.Authorization, "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
    }

    private fun remoteTableName(entityName: String): String = when (entityName) {
        "businesses" -> "negocios"
        "user_profiles" -> "perfiles"
        "miembros_negocio" -> "miembros_negocio"
        "products" -> "productos"
        "product_variants" -> "variantes_producto"
        "clients" -> "clientes"
        "suppliers" -> "proveedores"
        "sales" -> "ventas"
        "sale_items" -> "detalles_venta"
        "payments" -> "pagos"
        "debt_payments" -> "abonos_deuda"
        "expenses" -> "gastos"
        "cash_sessions" -> "sesiones_caja"
        "cash_movements" -> "movimientos_caja"
        else -> entityName
    }

    private fun getEntityPriority(entityName: String): Int {
        return when (entityName) {
            "businesses" -> 1
            "user_profiles", "miembros_negocio" -> 2
            "products" -> 3
            "product_variants" -> 4
            "clients", "suppliers" -> 5
            "sales" -> 6
            "sale_items" -> 7
            "payments" -> 8
            "debt_payments" -> 9
            "expenses" -> 10
            "cash_sessions", "cash_movements" -> 11
            "local_images" -> 12
            else -> 13
        }
    }
}
