package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Detalles de Venta (Tabla "detalles_venta").
 */
@Entity(
    tableName = "detalles_venta",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["saleId"])]
)
data class SaleItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val saleId: String,
    val productId: String,
    val variantId: String,
    val quantity: Int,
    val unitPriceCents: Long,
    val subtotalCents: Long,
    val createdAt: Long = System.currentTimeMillis()
)
