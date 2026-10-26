package com.example.dajho.domain.repository

import com.example.dajho.data.local.entity.CashMovementEntity
import com.example.dajho.data.local.entity.CashSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de Repositorio para Caja, Turnos y Movimientos (Fase 9).
 */
interface CashRepository {
    fun getActiveShift(): Flow<CashSessionEntity?>
    fun getClosedSessions(): Flow<List<CashSessionEntity>>
    fun getAllTransactions(): Flow<List<CashMovementEntity>>
    fun getMovementsForSession(sessionId: String): Flow<List<CashMovementEntity>>
    suspend fun openShift(initialCashCents: Long)
    suspend fun closeShift(sessionId: String, actualCashCents: Long, expectedCashCents: Long)
    suspend fun recordTransaction(transaction: CashMovementEntity)
}
