package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Arqueo y Apertura/Cierre de Caja (Fase 2).
 */
@Entity(tableName = "cash_shifts")
data class CashShiftEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val initialCashCents: Long,
    val expectedCashCents: Long,
    val actualCashCents: Long? = null,
    val isOpen: Boolean = true,
    val openedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT
)
