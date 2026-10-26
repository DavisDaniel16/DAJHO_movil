package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Abonos/Pagos de Deudas a Clientes o Proveedores (Fase 3 - RN-08).
 */
@Entity(tableName = "debt_payments")
data class DebtPaymentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val businessId: String = "DEFAULT_BUSINESS",
    val clientId: String? = null,
    val supplierId: String? = null,
    val amountCents: Long,
    val paymentMethod: String = "CASH",
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    val createdAt: Long = System.currentTimeMillis()
)
