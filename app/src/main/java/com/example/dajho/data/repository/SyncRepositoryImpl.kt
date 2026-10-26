package com.example.dajho.data.repository

import com.example.dajho.data.local.dao.SyncOperationDao
import com.example.dajho.data.local.entity.SyncOperationEntity
import com.example.dajho.domain.repository.SyncRepository

/**
 * Implementación del Repositorio de Cola de Sincronización respaldado por Room (Fase 4).
 */
class SyncRepositoryImpl(
    private val syncDao: SyncOperationDao
) : SyncRepository {

    override suspend fun enqueueOperation(
        entityName: String,
        entityId: String,
        operation: String,
        payloadJson: String
    ) {
        val op = SyncOperationEntity(
            entityName = entityName,
            entityId = entityId,
            operation = operation,
            payloadJson = payloadJson
        )
        syncDao.insertOperation(op)
    }

    override suspend fun getPendingOperations(): List<SyncOperationEntity> =
        syncDao.getAllPendingOperations()

    override suspend fun removeOperation(operationId: String) {
        syncDao.deleteOperation(operationId)
    }
}
