package com.example.dajho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.dajho.data.local.entity.CashMovementEntity
import com.example.dajho.data.local.entity.PaymentEntity
import com.example.dajho.data.local.entity.SaleEntity
import com.example.dajho.data.local.entity.SaleItemEntity
import com.example.dajho.data.local.entity.SyncOperationEntity
import com.example.dajho.data.local.relation.SaleWithItemsAndPayments
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Query("SELECT * FROM ventas ORDER BY timestamp DESC")
    fun getAllSalesFlow(): Flow<List<SaleEntity>>

    @Transaction
    @Query("SELECT * FROM ventas ORDER BY timestamp DESC")
    fun getAllSalesWithDetailsFlow(): Flow<List<SaleWithItemsAndPayments>>

    @Query("SELECT * FROM ventas WHERE id = :saleId")
    suspend fun getSaleById(saleId: String): SaleEntity?

    @Query("SELECT COALESCE(SUM(totalCents), 0) FROM ventas WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
    suspend fun getSalesTotalForDateRange(startOfDay: Long, endOfDay: Long): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashMovement(movement: CashMovementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncOperation(operation: SyncOperationEntity)

    @Query(
        "UPDATE variantes_producto SET stock = stock - :quantity, " +
            "updatedAt = :updatedAt, syncStatus = 'PENDING_UPDATE' " +
            "WHERE id = :variantId AND stock >= :quantity"
    )
    suspend fun deductVariantStock(
        variantId: String,
        quantity: Int,
        updatedAt: Long = System.currentTimeMillis()
    ): Int

    @Query("UPDATE clientes SET debtCents = debtCents + :addedDebtCents WHERE id = :clientId")
    suspend fun addClientDebt(clientId: String, addedDebtCents: Long)

    @Transaction
    suspend fun recordCompleteSale(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        payments: List<PaymentEntity>,
        activeShiftId: String? = null
    ) {
        insertSale(sale)
        insertSaleItems(items)
        insertPayments(payments)

        for (item in items) {
            require(item.quantity > 0) { "La cantidad debe ser mayor que cero" }
            val updatedRows = deductVariantStock(item.variantId, item.quantity)
            check(updatedRows == 1) {
                "Stock insuficiente o variante inexistente para ${item.variantId}"
            }
        }

        if (sale.paymentMethod == "CREDIT" && sale.clientId != null) {
            addClientDebt(sale.clientId, sale.totalCents)
        }

        if (activeShiftId != null && sale.paymentMethod == "CASH") {
            insertCashMovement(
                CashMovementEntity(
                    sessionId = activeShiftId,
                    concept = "Venta POS #${sale.saleNumber}",
                    amountCents = sale.totalCents,
                    movementType = "INGRESO",
                    relatedSaleId = sale.id
                )
            )
        }

        insertSyncOperation(
            SyncOperationEntity(
                entityName = "ventas",
                entityId = sale.id,
                operation = "INSERT",
                payloadJson = """
                    {
                      "id":"${sale.id}",
                      "negocio_id":"${sale.businessId}",
                      "usuario_id":"${sale.userId}",
                      "numero_venta":"${sale.saleNumber}",
                      "cliente_id":${sale.clientId?.let { "\"$it\"" } ?: "null"},
                      "metodo_pago":"${sale.paymentMethod}",
                      "referencia_transferencia":${sale.transferReference?.let { "\"$it\"" } ?: "null"},
                      "total_centavos":${sale.totalCents},
                      "marca_tiempo":${sale.timestamp},
                      "creado_en":"${java.time.Instant.ofEpochMilli(sale.createdAt)}",
                      "actualizado_en":"${java.time.Instant.ofEpochMilli(sale.updatedAt)}"
                    }
                """.trimIndent()
            )
        )
    }

    @Query("SELECT * FROM ventas WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSyncSales(): List<SaleEntity>
}
