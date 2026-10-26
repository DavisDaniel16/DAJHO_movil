package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Conflictos de Sincronización (Tabla "conflictos_sincronizacion").
 */
@Entity(tableName = "conflictos_sincronizacion")
data class SyncConflictEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val conflictType: String,
    val entityName: String,
    val entityId: String,
    val localPayloadJson: String,
    val remotePayloadJson: String = "",
    val resolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
