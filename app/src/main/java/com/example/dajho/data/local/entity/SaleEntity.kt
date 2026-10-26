package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Ventas (Tabla "ventas").
 */
@Entity(tableName = "ventas")
data class SaleEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val businessId: String = "DEFAULT_BUSINESS",
    val userId: String = "DEFAULT_USER",
    val saleNumber: String,
    val clientId: String? = null,
    val paymentMethod: String,
    val transferReference: String? = null,
    val totalCents: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
