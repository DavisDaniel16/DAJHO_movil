package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Pagos (Tabla "pagos").
 */
@Entity(
    tableName = "pagos",
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
data class PaymentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val saleId: String,
    val method: String,
    val amountCents: Long,
    val reference: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
