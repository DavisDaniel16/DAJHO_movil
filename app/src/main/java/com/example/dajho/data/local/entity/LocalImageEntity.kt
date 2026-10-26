package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Imágenes Locales (Tabla "imagenes_locales").
 */
@Entity(tableName = "imagenes_locales")
data class LocalImageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val localFilePath: String,
    val remoteStoragePath: String,
    val entityType: String,
    val entityId: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    val createdAt: Long = System.currentTimeMillis()
)
