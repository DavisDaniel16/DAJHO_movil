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

@Dao
interface ProductDao {

    @Query("SELECT * FROM productos ORDER BY name ASC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Transaction
    @Query("SELECT * FROM productos WHERE (deletedAt IS NULL OR deletedAt = 0) ORDER BY name ASC")
    fun getAllProductsWithVariantsFlow(): Flow<List<ProductWithVariants>>

<<<<<<< HEAD
    @Transaction
    @Query("SELECT * FROM productos WHERE businessId = :businessId AND (deletedAt IS NULL OR deletedAt = 0) ORDER BY name ASC")
    fun getProductsByBusinessFlow(businessId: String): Flow<List<ProductWithVariants>>

=======
>>>>>>> 0f93a4a83ec40e8ea28347f46453a5399c98f3e6
    @Query("SELECT * FROM productos WHERE id = :productId")
    suspend fun getProductById(productId: String): ProductEntity?

    @Query("SELECT * FROM productos WHERE LOWER(sku) = LOWER(:sku) AND (deletedAt IS NULL OR deletedAt = 0) LIMIT 1")
    suspend fun getProductBySku(sku: String): ProductEntity?

    @Query("SELECT COUNT(*) FROM variantes_producto WHERE stock <= 3")
    suspend fun getLowStockVariantsCount(): Int

    @Query("SELECT * FROM variantes_producto WHERE productId = :productId")
    fun getVariantsByProductIdFlow(productId: String): Flow<List<ProductVariantEntity>>

    @Query("SELECT * FROM variantes_producto WHERE productId = :productId")
    suspend fun getVariantsByProductId(productId: String): List<ProductVariantEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariants(variants: List<ProductVariantEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Update
    suspend fun updateVariant(variant: ProductVariantEntity)

    @Query("UPDATE productos SET deletedAt = :deletedAt, syncStatus = 'PENDING_DELETE' WHERE id = :productId")
    suspend fun softDeleteProduct(productId: String, deletedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM variantes_producto WHERE productId = :productId")
    suspend fun deleteVariantsByProductId(productId: String)

    @Query("SELECT * FROM productos WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSyncProducts(): List<ProductEntity>
}
