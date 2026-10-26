package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Operaciones de Sincronización (Tabla "operaciones_sincronizacion").
 */
@Entity(tableName = "operaciones_sincronizacion")
data class SyncOperationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val entityName: String,
    val entityId: String,
    val operation: String,
    val payloadJson: String,
    val attempts: Int = 0,
    val lastError: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
