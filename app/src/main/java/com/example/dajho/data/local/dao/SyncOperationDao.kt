package com.example.dajho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dajho.data.local.entity.SyncOperationEntity

@Dao
interface SyncOperationDao {

    @Query("SELECT * FROM operaciones_sincronizacion ORDER BY createdAt ASC")
    suspend fun getAllPendingOperations(): List<SyncOperationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: SyncOperationEntity)

    @Update
    suspend fun updateOperation(operation: SyncOperationEntity)

    @Query("UPDATE operaciones_sincronizacion SET attempts = attempts + 1, lastError = :errorMsg WHERE id = :operationId")
    suspend fun recordErrorAttempt(operationId: String, errorMsg: String)

    @Query("DELETE FROM operaciones_sincronizacion WHERE id = :operationId")
    suspend fun deleteOperation(operationId: String)
}
