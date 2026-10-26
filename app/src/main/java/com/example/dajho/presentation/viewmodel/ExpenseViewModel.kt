package com.example.dajho.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dajho.data.local.entity.ExpenseEntity
import com.example.dajho.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de Gastos y Egresos Operativos (Fase 4).
 */
class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    val expensesState: StateFlow<List<ExpenseEntity>> = repository.getAllExpenses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.saveExpense(expense)
        }
    }
}
