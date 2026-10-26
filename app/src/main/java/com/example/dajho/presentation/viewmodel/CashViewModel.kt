package com.example.dajho.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dajho.data.local.entity.CashMovementEntity
import com.example.dajho.data.local.entity.CashSessionEntity
import com.example.dajho.domain.repository.CashRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de Balance, Caja y Arqueo (Fase 9).
 */
class CashViewModel(
    private val repository: CashRepository
) : ViewModel() {

    val activeShiftState: StateFlow<CashSessionEntity?> = repository.getActiveShift()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val closedSessionsState: StateFlow<List<CashSessionEntity>> = repository.getClosedSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val transactionsState: StateFlow<List<CashMovementEntity>> = repository.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun openShift(initialCashCents: Long) {
        viewModelScope.launch {
            repository.openShift(initialCashCents)
        }
    }

    fun closeShift(sessionId: String, actualCashCents: Long, expectedCashCents: Long) {
        viewModelScope.launch {
            repository.closeShift(sessionId, actualCashCents, expectedCashCents)
        }
    }

    fun recordExpense(sessionId: String, concept: String, amountCents: Long) {
        viewModelScope.launch {
            val tx = CashMovementEntity(
                sessionId = sessionId,
                concept = concept,
                amountCents = amountCents,
                movementType = "EGRESO"
            )
            repository.recordTransaction(tx)
        }
    }
}
