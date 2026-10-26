package com.example.dajho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dajho.data.local.entity.LocalImageEntity

/**
 * DAO Room para Imágenes Pendientes de Subida a Storage (Fase 3).
 */
@Dao
interface LocalImageDao {

    @Query("SELECT * FROM local_images WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingImages(): List<LocalImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: LocalImageEntity)
}
