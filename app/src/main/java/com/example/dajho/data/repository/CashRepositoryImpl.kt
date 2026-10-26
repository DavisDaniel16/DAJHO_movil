package com.example.dajho.data.repository

import com.example.dajho.data.local.dao.CashDao
import com.example.dajho.data.local.dao.SyncOperationDao
import com.example.dajho.data.local.entity.CashMovementEntity
import com.example.dajho.data.local.entity.CashSessionEntity
import com.example.dajho.data.local.entity.SyncOperationEntity
import com.example.dajho.domain.repository.CashRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación del Repositorio de Caja respaldado por Room y Cola Sync (Fase 9).
 */
class CashRepositoryImpl(
    private val cashDao: CashDao,
    private val syncDao: SyncOperationDao
) : CashRepository {

    override fun getActiveShift(): Flow<CashSessionEntity?> = cashDao.getActiveShiftFlow()

    override fun getClosedSessions(): Flow<List<CashSessionEntity>> = cashDao.getClosedSessionsFlow()

    override fun getAllTransactions(): Flow<List<CashMovementEntity>> = cashDao.getAllTransactionsFlow()

    override fun getMovementsForSession(sessionId: String): Flow<List<CashMovementEntity>> =
        cashDao.getMovementsForSessionFlow(sessionId)

    override suspend fun openShift(initialCashCents: Long) {
        val shift = CashSessionEntity(
            initialCashCents = initialCashCents,
            expectedCashCents = initialCashCents
        )
        cashDao.insertShift(shift)
        syncDao.insertOperation(
            SyncOperationEntity(
                entityName = "cash_sessions",
                entityId = shift.id,
                operation = "INSERT",
                payloadJson = "{\"initialCashCents\":$initialCashCents}"
            )
        )
    }

    override suspend fun closeShift(sessionId: String, actualCashCents: Long, expectedCashCents: Long) {
        cashDao.closeShiftTransaction(sessionId, actualCashCents, expectedCashCents)
    }

    override suspend fun recordTransaction(transaction: CashMovementEntity) {
        cashDao.insertTransaction(transaction)
        syncDao.insertOperation(
            SyncOperationEntity(
                entityName = "cash_movements",
                entityId = transaction.id,
                operation = "INSERT",
                payloadJson = "{\"concept\":\"${transaction.concept}\",\"amountCents\":${transaction.amountCents}}"
            )
        )
    }
}
