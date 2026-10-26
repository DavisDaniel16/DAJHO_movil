package com.example.dajho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dajho.data.local.entity.SyncOperationEntity

/**
 * DAO Room para Cola de Sincronización Offline y Auditoría de Reintentos (Fase 13).
 */
@Dao
interface SyncOperationDao {

    @Query("SELECT * FROM sync_operations ORDER BY createdAt ASC")
    suspend fun getAllPendingOperations(): List<SyncOperationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: SyncOperationEntity)

    @Update
    suspend fun updateOperation(operation: SyncOperationEntity)

    @Query("UPDATE sync_operations SET attempts = attempts + 1, lastError = :errorMsg WHERE id = :operationId")
    suspend fun recordErrorAttempt(operationId: String, errorMsg: String)

    @Query("DELETE FROM sync_operations WHERE id = :operationId")
    suspend fun deleteOperation(operationId: String)
}
