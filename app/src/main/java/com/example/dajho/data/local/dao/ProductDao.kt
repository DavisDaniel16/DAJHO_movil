package com.example.dajho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.data.local.entity.ProductVariantEntity
import com.example.dajho.data.local.relation.ProductWithVariants
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room para Productos, Variantes y Métricas de Inventario (Fase 10).
 */
@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Transaction
    @Query("SELECT * FROM products WHERE (deletedAt IS NULL OR deletedAt = 0) ORDER BY name ASC")
    fun getAllProductsWithVariantsFlow(): Flow<List<ProductWithVariants>>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: String): ProductEntity?

    @Query("SELECT * FROM products WHERE LOWER(sku) = LOWER(:sku) AND (deletedAt IS NULL OR deletedAt = 0) LIMIT 1")
    suspend fun getProductBySku(sku: String): ProductEntity?

    @Query("SELECT COUNT(*) FROM product_variants WHERE stock <= 3")
    suspend fun getLowStockVariantsCount(): Int

    @Query("SELECT * FROM product_variants WHERE productId = :productId")
    fun getVariantsByProductIdFlow(productId: String): Flow<List<ProductVariantEntity>>

    @Query("SELECT * FROM product_variants WHERE productId = :productId")
    suspend fun getVariantsByProductId(productId: String): List<ProductVariantEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariants(variants: List<ProductVariantEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Update
    suspend fun updateVariant(variant: ProductVariantEntity)

    @Query("UPDATE products SET deletedAt = :deletedAt, syncStatus = 'PENDING_DELETE' WHERE id = :productId")
    suspend fun softDeleteProduct(productId: String, deletedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM product_variants WHERE productId = :productId")
    suspend fun deleteVariantsByProductId(productId: String)

    @Query("SELECT * FROM products WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSyncProducts(): List<ProductEntity>
}
