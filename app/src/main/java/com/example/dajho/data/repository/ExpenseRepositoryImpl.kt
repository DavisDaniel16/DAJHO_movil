package com.example.dajho.data.repository

import com.example.dajho.data.local.dao.ExpenseDao
import com.example.dajho.data.local.dao.SyncOperationDao
import com.example.dajho.data.local.entity.ExpenseEntity
import com.example.dajho.data.local.entity.SyncOperationEntity
import com.example.dajho.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación del Repositorio de Gastos respaldado por Room y Cola Sync (Fase 4).
 */
class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao,
    private val syncDao: SyncOperationDao
) : ExpenseRepository {

    override fun getAllExpenses(businessId: String): Flow<List<ExpenseEntity>> =
        expenseDao.getAllExpensesFlow(businessId)

    override suspend fun saveExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
        syncDao.insertOperation(
            SyncOperationEntity(
                entityName = "expenses",
                entityId = expense.id,
                operation = "INSERT",
                payloadJson = "{\"concept\":\"${expense.concept}\"}"
            )
        )
    }
}
