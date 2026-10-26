package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Imágenes locales pendientes de subir a Supabase Storage (Fase 3).
 */
@Entity(tableName = "local_images")
data class LocalImageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val localFilePath: String,
    val remoteStoragePath: String,
    val entityType: String, // PRODUCT, RECEIPT
    val entityId: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    val createdAt: Long = System.currentTimeMillis()
)
