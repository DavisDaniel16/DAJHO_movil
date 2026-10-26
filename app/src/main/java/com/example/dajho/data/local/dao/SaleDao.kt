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

/**
 * DAO Room para Ventas, Detalle y Agregaciones del Dashboard (Fase 10).
 */
@Dao
interface SaleDao {

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSalesFlow(): Flow<List<SaleEntity>>

    @Transaction
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSalesWithDetailsFlow(): Flow<List<SaleWithItemsAndPayments>>

    @Query("SELECT * FROM sales WHERE id = :saleId")
    suspend fun getSaleById(saleId: String): SaleEntity?

    @Query("SELECT COALESCE(SUM(totalCents), 0) FROM sales WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
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

    @Query("UPDATE product_variants SET stock = MAX(0, stock - :quantity) WHERE id = :variantId")
    suspend fun deductVariantStock(variantId: String, quantity: Int)

    @Query("UPDATE clients SET debtCents = debtCents + :addedDebtCents WHERE id = :clientId")
    suspend fun addClientDebt(clientId: String, addedDebtCents: Long)

    /**
     * Transacción atómica SQLite completa para finalizar venta (Fase 7).
     */
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
            deductVariantStock(item.variantId, item.quantity)
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
                entityName = "sales",
                entityId = sale.id,
                operation = "INSERT",
                payloadJson = "{\"saleNumber\":\"${sale.saleNumber}\",\"totalCents\":${sale.totalCents}}"
            )
        )
    }

    @Query("SELECT * FROM sales WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSyncSales(): List<SaleEntity>
}
