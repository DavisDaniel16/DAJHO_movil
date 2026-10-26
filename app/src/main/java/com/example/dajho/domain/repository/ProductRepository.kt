package com.example.dajho.domain.repository

import com.example.dajho.data.local.entity.LocalImageEntity
import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.data.local.entity.ProductVariantEntity
import com.example.dajho.data.local.relation.ProductWithVariants
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de Repositorio para Productos y Variantes (Fase 6).
 */
interface ProductRepository {
    fun getAllProductsWithVariantsFlow(): Flow<List<ProductWithVariants>>
    suspend fun getProductById(productId: String): ProductEntity?
    suspend fun getProductBySku(sku: String): ProductEntity?
    suspend fun saveProductWithImage(
        product: ProductEntity,
        variants: List<ProductVariantEntity>,
        localImage: LocalImageEntity? = null
    )
    suspend fun softDeleteProduct(productId: String)
}
