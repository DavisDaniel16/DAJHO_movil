package com.example.dajho.cash

/**
 * Modelo de datos para transacciones de Ingresos y Egresos (Fase 3).
 */
data class CashTransaction(
    val id: Long,
    val concept: String,
    val amount: Double,
    val timestamp: String,
    val isIncome: Boolean,
    val category: String
)
