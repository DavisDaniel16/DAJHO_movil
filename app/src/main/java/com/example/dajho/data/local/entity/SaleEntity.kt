package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Ventas y Notas de Venta RIMPE (Fase 3).
 */
@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val businessId: String = "DEFAULT_BUSINESS",
    val userId: String = "DEFAULT_USER",
    val saleNumber: String,
    val clientId: String? = null, // Nulo para Consumidor Final
    val paymentMethod: String, // CASH, TRANSFER, CREDIT
    val transferReference: String? = null,
    val totalCents: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
