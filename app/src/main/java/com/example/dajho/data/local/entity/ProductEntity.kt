package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Productos en inventario (Fase 3).
 * IDs en UUID string, montos en céntimos enteros (Long) y soft-delete deletedAt.
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val businessId: String = "DEFAULT_BUSINESS",
    val sku: String,
    val name: String,
    val brand: String,
    val category: String,
    val costCents: Long,
    val salePriceCents: Long,
    val imageUri: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
