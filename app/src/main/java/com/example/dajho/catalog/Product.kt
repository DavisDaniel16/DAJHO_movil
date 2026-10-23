package com.example.dajho.catalog

/**
 * Modelo de UI para el catalogo (Fase 1).
 * Solo frontend: los datos viven en memoria para simular el API.
 */
data class Product(
    val id: Long,
    val sku: String,
    val name: String,
    val brand: String,
    val category: String,
    val cost: Double,
    val salePrice: Double,
    val imageUri: String? = null,
    val variants: MutableList<ProductVariant> = mutableListOf()
) {
    fun totalStock(): Int = variants.sumOf { it.stock }

    fun variantCount(): Int = variants.size

    fun sizesSummary(): String =
        variants.map { it.size }.filter { it.isNotBlank() }.distinct().joinToString(", ")

    fun colorsSummary(): String =
        variants.map { it.color }.filter { it.isNotBlank() }.distinct().joinToString(", ")
}

data class ProductVariant(
    val id: Long,
    var size: String,
    var color: String,
    var stock: Int
)
