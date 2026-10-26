package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Movimientos de Caja (Tabla "movimientos_caja").
 */
@Entity(
    tableName = "movimientos_caja",
    foreignKeys = [
        ForeignKey(
            entity = CashSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class CashMovementEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val concept: String,
    val amountCents: Long,
    val movementType: String, // INGRESO, EGRESO
    val relatedSaleId: String? = null,
    val relatedClientId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
