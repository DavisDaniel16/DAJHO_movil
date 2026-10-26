package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Auditoría y Revisión Manual de Conflictos de Sincronización (Fase 15).
 */
@Entity(tableName = "sync_conflicts")
data class SyncConflictEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val conflictType: String, // OVERSELL_STOCK_WARNING, PRODUCT_PRICE_MISMATCH, PULL_COLLISION
    val entityName: String,
    val entityId: String,
    val localPayloadJson: String,
    val remotePayloadJson: String = "",
    val resolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
