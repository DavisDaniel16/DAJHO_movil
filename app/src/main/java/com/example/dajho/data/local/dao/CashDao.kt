package com.example.dajho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.dajho.data.local.entity.CashMovementEntity
import com.example.dajho.data.local.entity.CashSessionEntity
import com.example.dajho.data.local.entity.SyncOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashDao {

    @Query("SELECT * FROM sesiones_caja WHERE isOpen = 1 ORDER BY openedAt DESC LIMIT 1")
    fun getActiveShiftFlow(): Flow<CashSessionEntity?>

    @Query("SELECT * FROM sesiones_caja WHERE isOpen = 0 ORDER BY closedAt DESC")
    fun getClosedSessionsFlow(): Flow<List<CashSessionEntity>>

    @Query("SELECT * FROM movimientos_caja ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<CashMovementEntity>>

    @Query("SELECT * FROM movimientos_caja WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getMovementsForSessionFlow(sessionId: String): Flow<List<CashMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: CashSessionEntity)

    @Update
    suspend fun updateShift(shift: CashSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CashMovementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncOperation(operation: SyncOperationEntity)

    @Query("UPDATE sesiones_caja SET isOpen = 0, actualCashCents = :actualCashCents, expectedCashCents = :expectedCashCents, closedAt = :closedAt WHERE id = :sessionId")
    suspend fun closeShiftQuery(sessionId: String, actualCashCents: Long, expectedCashCents: Long, closedAt: Long)

    @Transaction
    suspend fun closeShiftTransaction(sessionId: String, actualCashCents: Long, expectedCashCents: Long, closedAt: Long = System.currentTimeMillis()) {
        closeShiftQuery(sessionId, actualCashCents, expectedCashCents, closedAt)
        insertSyncOperation(
            SyncOperationEntity(
                entityName = "sesiones_caja",
                entityId = sessionId,
                operation = "UPDATE",
                payloadJson = "{\"actualCashCents\":$actualCashCents,\"expectedCashCents\":$expectedCashCents}"
            )
        )
    }
}
