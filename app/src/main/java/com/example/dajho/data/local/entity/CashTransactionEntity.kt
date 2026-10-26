package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Transacciones de Caja (Ingresos y Egresos) (Fase 2).
 */
@Entity(tableName = "cash_transactions")
data class CashTransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val shiftId: String? = null,
    val concept: String,
    val category: String,
    val amountCents: Long,
    val isIncome: Boolean,
    val relatedSaleId: String? = null,
    val relatedClientId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT
)
