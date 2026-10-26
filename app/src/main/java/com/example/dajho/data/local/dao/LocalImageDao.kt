package com.example.dajho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dajho.data.local.entity.LocalImageEntity

@Dao
interface LocalImageDao {

    @Query("SELECT * FROM imagenes_locales WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingImages(): List<LocalImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: LocalImageEntity)

    @Query(
        "UPDATE imagenes_locales SET syncStatus = 'PENDING_INSERT' " +
            "WHERE id = :imageId"
    )
    suspend fun recordUploadError(imageId: String)

    @Query("UPDATE imagenes_locales SET syncStatus = 'SYNCED' WHERE id = :imageId")
    suspend fun markSynced(imageId: String)
}
