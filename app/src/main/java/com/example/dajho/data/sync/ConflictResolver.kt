package com.example.dajho.data.sync

import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.data.local.entity.SyncConflictEntity

/**
 * Motor de resolución de conflictos según la política oficial DAJHO (Fase 15).
 */
object ConflictResolver {

    /**
     * Resuelve conflicto Last-Write-Wins (LWW) para edición concurrente de productos.
     */
    fun resolveProductConflictLww(
        localProduct: ProductEntity,
        remoteProduct: ProductEntity
    ): ProductEntity {
        return if (remoteProduct.updatedAt > localProduct.updatedAt) {
            remoteProduct
        } else {
            localProduct
        }
    }

    /**
     * Detecta sobreventa de stock en variantes y registra una alerta en sync_conflicts.
     */
    suspend fun flagOversellStockWarning(
        db: AppDatabase,
        productId: String,
        variantId: String,
        remainingStock: Int
    ) {
        if (remainingStock < 0) {
            val conflict = SyncConflictEntity(
                conflictType = "OVERSELL_STOCK_WARNING",
                entityName = "product_variants",
                entityId = variantId,
                localPayloadJson = "{\"productId\":\"$productId\",\"remainingStock\":$remainingStock}",
                remotePayloadJson = "Alerta: Sobreventa detectada offline"
            )
            db.syncConflictDao().insertConflict(conflict)
        }
    }
}
