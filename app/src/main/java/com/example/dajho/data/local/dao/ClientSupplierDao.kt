package com.example.dajho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.dajho.data.local.entity.CashMovementEntity
import com.example.dajho.data.local.entity.ClientEntity
import com.example.dajho.data.local.entity.DebtPaymentEntity
import com.example.dajho.data.local.entity.SupplierEntity
import com.example.dajho.data.local.entity.SyncOperationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room para Clientes, Proveedores y Métricas de Deudas (Fase 10).
 */
@Dao
interface ClientSupplierDao {

    @Query("SELECT * FROM clients WHERE businessId = :businessId AND deletedAt IS NULL ORDER BY name ASC")
    fun getAllClientsFlow(businessId: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :clientId")
    suspend fun getClientById(clientId: String): ClientEntity?

    @Query("SELECT COUNT(*) FROM clients WHERE debtCents > 0 AND deletedAt IS NULL")
    suspend fun getDebtorsCount(): Int

    @Query("SELECT COALESCE(SUM(debtCents), 0) FROM clients WHERE deletedAt IS NULL")
    suspend fun getTotalClientsDebt(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity)

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Query("UPDATE clients SET debtCents = MAX(0, debtCents - :paymentCents) WHERE id = :clientId")
    suspend fun deductClientDebt(clientId: String, paymentCents: Long)

    @Query("SELECT * FROM suppliers WHERE businessId = :businessId AND deletedAt IS NULL ORDER BY name ASC")
    fun getAllSuppliersFlow(businessId: String): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :supplierId")
    suspend fun getSupplierById(supplierId: String): SupplierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity)

    @Query("UPDATE suppliers SET debtCents = MAX(0, debtCents - :paymentCents) WHERE id = :supplierId")
    suspend fun deductSupplierDebt(supplierId: String, paymentCents: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtPayment(payment: DebtPaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashMovement(movement: CashMovementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncOperation(operation: SyncOperationEntity)

    @Query("SELECT * FROM debt_payments WHERE clientId = :clientId ORDER BY timestamp DESC")
    fun getClientDebtPaymentsFlow(clientId: String): Flow<List<DebtPaymentEntity>>

    /**
     * Transacción Atómica SQLite para Abono de Cliente (Fase 8 - RN-08).
     */
    @Transaction
    suspend fun recordClientDebtPaymentTransaction(
        payment: DebtPaymentEntity,
        activeShiftId: String? = null
    ) {
        insertDebtPayment(payment)
        if (payment.clientId != null) {
            deductClientDebt(payment.clientId, payment.amountCents)
        }

        if (activeShiftId != null && payment.paymentMethod == "CASH") {
            insertCashMovement(
                CashMovementEntity(
                    sessionId = activeShiftId,
                    concept = "Abono Deuda Cliente",
                    amountCents = payment.amountCents,
                    movementType = "INGRESO",
                    relatedClientId = payment.clientId
                )
            )
        }

        insertSyncOperation(
            SyncOperationEntity(
                entityName = "debt_payments",
                entityId = payment.id,
                operation = "INSERT",
                payloadJson = "{\"amountCents\":${payment.amountCents}}"
            )
        )
    }

    /**
     * Transacción Atómica SQLite para Pago a Proveedor (Fase 8).
     */
    @Transaction
    suspend fun recordSupplierPaymentTransaction(payment: DebtPaymentEntity) {
        insertDebtPayment(payment)
        if (payment.supplierId != null) {
            deductSupplierDebt(payment.supplierId, payment.amountCents)
        }

        insertSyncOperation(
            SyncOperationEntity(
                entityName = "debt_payments",
                entityId = payment.id,
                operation = "INSERT",
                payloadJson = "{\"amountCents\":${payment.amountCents}}"
            )
        )
    }
}
