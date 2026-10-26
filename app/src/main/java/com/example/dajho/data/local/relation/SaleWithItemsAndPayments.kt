package com.example.dajho.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.dajho.data.local.entity.PaymentEntity
import com.example.dajho.data.local.entity.SaleEntity
import com.example.dajho.data.local.entity.SaleItemEntity

/**
 * Relación Room de Venta Completa con Items y Formas de Pago (Fase 3).
 */
data class SaleWithItemsAndPayments(
    @Embedded val sale: SaleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val items: List<SaleItemEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val payments: List<PaymentEntity>
)
