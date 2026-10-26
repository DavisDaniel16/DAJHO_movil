package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Gastos/Egresos Operativos del Comercio (Fase 3).
 */
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val businessId: String = "DEFAULT_BUSINESS",
    val category: String, // Mercadería, Arriendo, Servicios, Sueldos, Varios
    val concept: String,
    val amountCents: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
