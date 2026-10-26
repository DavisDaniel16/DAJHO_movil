package com.example.dajho.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.data.local.entity.ProductVariantEntity

/**
 * Relación Room de Producto con sus Variantes (Fase 3).
 */
data class ProductWithVariants(
    @Embedded val product: ProductEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val variants: List<ProductVariantEntity>
)
