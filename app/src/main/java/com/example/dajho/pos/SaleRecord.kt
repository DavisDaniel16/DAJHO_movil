package com.example.dajho.pos

import com.example.dajho.clients.Client

/**
 * Modelo de registro de Venta finalizada y Nota de Venta RIMPE (Fase 3).
 */
data class SaleRecord(
    val saleId: String,
    val timestamp: String,
    val items: List<CartItem>,
    val paymentMethod: PaymentMethod,
    val client: Client?,
    val transferReference: String? = null,
    val totalAmount: Double
)

enum class PaymentMethod(val displayName: String) {
    CASH("Efectivo"),
    TRANSFER("Transferencia"),
    CREDIT("Fiado / Crédito")
}
