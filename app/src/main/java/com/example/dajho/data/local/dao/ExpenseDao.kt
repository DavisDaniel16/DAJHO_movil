package com.example.dajho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dajho.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room para Gastos y Egresos Operativos (Fase 3).
 */
@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE businessId = :businessId AND deletedAt IS NULL ORDER BY timestamp DESC")
    fun getAllExpensesFlow(businessId: String): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)
}
