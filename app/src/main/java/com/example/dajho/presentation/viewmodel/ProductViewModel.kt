package com.example.dajho.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dajho.data.local.entity.LocalImageEntity
import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.data.local.entity.ProductVariantEntity
import com.example.dajho.data.local.relation.ProductWithVariants
import com.example.dajho.domain.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de Productos e Inventario (Fase 6).
 */
class ProductViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    val productsState: StateFlow<List<ProductWithVariants>> = repository.getAllProductsWithVariantsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun checkSkuExists(sku: String, currentProductId: String?): Boolean {
        val existing = repository.getProductBySku(sku) ?: return false
        return existing.id != currentProductId
    }

    fun saveProductWithImage(
        product: ProductEntity,
        variants: List<ProductVariantEntity>,
        localImage: LocalImageEntity?
    ) {
        viewModelScope.launch {
            repository.saveProductWithImage(product, variants, localImage)
        }
    }

    fun softDeleteProduct(productId: String) {
        viewModelScope.launch {
            repository.softDeleteProduct(productId)
        }
    }
}
