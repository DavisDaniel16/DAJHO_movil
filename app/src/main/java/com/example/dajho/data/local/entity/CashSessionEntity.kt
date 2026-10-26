package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Sesiones de Caja (Tabla "sesiones_caja").
 */
@Entity(tableName = "sesiones_caja")
data class CashSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val businessId: String = "DEFAULT_BUSINESS",
    val userId: String = "DEFAULT_USER",
    val initialCashCents: Long,
    val expectedCashCents: Long,
    val actualCashCents: Long? = null,
    val isOpen: Boolean = true,
    val openedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
