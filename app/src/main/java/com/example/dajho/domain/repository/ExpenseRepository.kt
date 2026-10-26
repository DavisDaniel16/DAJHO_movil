package com.example.dajho.domain.repository

import com.example.dajho.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de Repositorio para Egresos y Gastos (Fase 4).
 */
interface ExpenseRepository {
    fun getAllExpenses(businessId: String = "DEFAULT_BUSINESS"): Flow<List<ExpenseEntity>>
    suspend fun saveExpense(expense: ExpenseEntity)
}
