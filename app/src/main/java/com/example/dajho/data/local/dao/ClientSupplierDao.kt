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

@Dao
interface ClientSupplierDao {

    @Query("SELECT * FROM clientes WHERE businessId = :businessId AND deletedAt IS NULL ORDER BY name ASC")
    fun getAllClientsFlow(businessId: String): Flow<List<ClientEntity>>

<<<<<<< HEAD
    @Query("SELECT * FROM clientes WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSyncClients(): List<ClientEntity>

=======
>>>>>>> 0f93a4a83ec40e8ea28347f46453a5399c98f3e6
    @Query("SELECT * FROM clientes WHERE id = :clientId")
    suspend fun getClientById(clientId: String): ClientEntity?

    @Query("SELECT COUNT(*) FROM clientes WHERE debtCents > 0 AND deletedAt IS NULL")
    suspend fun getDebtorsCount(): Int

    @Query("SELECT COALESCE(SUM(debtCents), 0) FROM clientes WHERE deletedAt IS NULL")
    suspend fun getTotalClientsDebt(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity)

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Query("UPDATE clientes SET debtCents = MAX(0, debtCents - :paymentCents) WHERE id = :clientId")
    suspend fun deductClientDebt(clientId: String, paymentCents: Long)

    @Query("SELECT * FROM proveedores WHERE businessId = :businessId AND deletedAt IS NULL ORDER BY name ASC")
    fun getAllSuppliersFlow(businessId: String): Flow<List<SupplierEntity>>

<<<<<<< HEAD
    @Query("SELECT * FROM proveedores WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSyncSuppliers(): List<SupplierEntity>

=======
>>>>>>> 0f93a4a83ec40e8ea28347f46453a5399c98f3e6
    @Query("SELECT * FROM proveedores WHERE id = :supplierId")
    suspend fun getSupplierById(supplierId: String): SupplierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity)

    @Query("UPDATE proveedores SET debtCents = MAX(0, debtCents - :paymentCents) WHERE id = :supplierId")
    suspend fun deductSupplierDebt(supplierId: String, paymentCents: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtPayment(payment: DebtPaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashMovement(movement: CashMovementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncOperation(operation: SyncOperationEntity)

    @Query("SELECT * FROM abonos_deuda WHERE clientId = :clientId ORDER BY timestamp DESC")
    fun getClientDebtPaymentsFlow(clientId: String): Flow<List<DebtPaymentEntity>>

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
                entityName = "abonos_deuda",
                entityId = payment.id,
                operation = "INSERT",
                payloadJson = "{\"amountCents\":${payment.amountCents}}"
            )
        )
    }

    @Transaction
    suspend fun recordSupplierPaymentTransaction(payment: DebtPaymentEntity) {
        insertDebtPayment(payment)
        if (payment.supplierId != null) {
            deductSupplierDebt(payment.supplierId, payment.amountCents)
        }

        insertSyncOperation(
            SyncOperationEntity(
                entityName = "abonos_deuda",
                entityId = payment.id,
                operation = "INSERT",
                payloadJson = "{\"amountCents\":${payment.amountCents}}"
            )
        )
    }
}
