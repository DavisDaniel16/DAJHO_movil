package com.example.dajho.pos

import com.example.dajho.catalog.Product
import com.example.dajho.catalog.ProductVariant

/**
 * Representa un item en el carrito de compras (Fase 3 - POS).
 */
data class CartItem(
    val product: Product,
    val variant: ProductVariant,
    var quantity: Int = 1
) {
    fun subtotal(): Double = product.salePrice * quantity

    fun variantDescription(): String =
        listOf(variant.size, variant.color).filter { it.isNotBlank() }.joinToString(" / ")
}
