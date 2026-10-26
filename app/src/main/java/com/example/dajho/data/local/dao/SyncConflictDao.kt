package com.example.dajho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dajho.data.local.entity.SyncConflictEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room para Registro y Resolución de Conflictos (Fase 15).
 */
@Dao
interface SyncConflictDao {

    @Query("SELECT * FROM sync_conflicts WHERE resolved = 0 ORDER BY createdAt DESC")
    fun getUnresolvedConflictsFlow(): Flow<List<SyncConflictEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConflict(conflict: SyncConflictEntity)

    @Query("UPDATE sync_conflicts SET resolved = 1 WHERE id = :conflictId")
    suspend fun resolveConflict(conflictId: String)
}
