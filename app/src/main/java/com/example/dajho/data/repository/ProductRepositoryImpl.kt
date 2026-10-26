package com.example.dajho.data.repository

import com.example.dajho.data.local.dao.LocalImageDao
import com.example.dajho.data.local.dao.ProductDao
import com.example.dajho.data.local.dao.SyncOperationDao
import com.example.dajho.data.local.entity.LocalImageEntity
import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.data.local.entity.ProductVariantEntity
import com.example.dajho.data.local.entity.SyncOperationEntity
import com.example.dajho.data.local.relation.ProductWithVariants
import com.example.dajho.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

/**
 * Implementación del Repositorio de Productos respaldado por Room y Cola Sync (Fase 6).
 */
class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val syncDao: SyncOperationDao,
    private val imageDao: LocalImageDao
) : ProductRepository {

    override fun getAllProductsWithVariantsFlow(): Flow<List<ProductWithVariants>> =
        productDao.getAllProductsWithVariantsFlow()

    override suspend fun getProductById(productId: String): ProductEntity? =
        productDao.getProductById(productId)

    override suspend fun getProductBySku(sku: String): ProductEntity? =
        productDao.getProductBySku(sku)

    override suspend fun saveProductWithImage(
        product: ProductEntity,
        variants: List<ProductVariantEntity>,
        localImage: LocalImageEntity?
    ) {
        productDao.insertProduct(product)
        productDao.deleteVariantsByProductId(product.id)
        productDao.insertVariants(variants)

        if (localImage != null) {
            imageDao.insertImage(localImage)
        }

        syncDao.insertOperation(
            SyncOperationEntity(
                entityName = "products",
                entityId = product.id,
                operation = "INSERT",
                payloadJson = JSONObject().apply {
                    put("id", product.id)
                    put("negocio_id", product.businessId)
                    put("sku", product.sku)
                    put("nombre", product.name)
                    put("marca", product.brand)
                    put("categoria", product.category)
                    put("costo_centavos", product.costCents)
                    put("precio_venta_centavos", product.salePriceCents)
                    put("uri_imagen", product.imageUri ?: JSONObject.NULL)
                }.toString()
            )
        )

        for (v in variants) {
            syncDao.insertOperation(
                SyncOperationEntity(
                    entityName = "product_variants",
                    entityId = v.id,
                    operation = "INSERT",
                    payloadJson = JSONObject().apply {
                        put("id", v.id)
                        put("producto_id", v.productId)
                        put("talla", v.size)
                        put("color", v.color)
                        put("stock", v.stock)
                    }.toString()
                )
            )
        }
    }

    override suspend fun softDeleteProduct(productId: String) {
        productDao.softDeleteProduct(productId)
        syncDao.insertOperation(
            SyncOperationEntity(
                entityName = "products",
                entityId = productId,
                operation = "DELETE",
                payloadJson = JSONObject().apply {
                    put("deletedAt", System.currentTimeMillis())
                }.toString()
            )
        )
    }
}
