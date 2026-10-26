package com.example.dajho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dajho.data.local.entity.SyncConflictEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncConflictDao {

    @Query("SELECT * FROM conflictos_sincronizacion WHERE resolved = 0 ORDER BY createdAt DESC")
    fun getUnresolvedConflictsFlow(): Flow<List<SyncConflictEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConflict(conflict: SyncConflictEntity)

    @Query("UPDATE conflictos_sincronizacion SET resolved = 1 WHERE id = :conflictId")
    suspend fun resolveConflict(conflictId: String)
}
