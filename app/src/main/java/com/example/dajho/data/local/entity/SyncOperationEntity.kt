package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Cola de Operaciones Pendientes de Sincronización Offline (Fase 13).
 */
@Entity(tableName = "sync_operations")
data class SyncOperationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val entityName: String, // products, clients, sales, expenses, etc.
    val entityId: String,
    val operation: String, // INSERT, UPDATE, DELETE
    val payloadJson: String,
    val attempts: Int = 0,
    val lastError: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
