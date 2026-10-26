package com.example.dajho.domain.repository

import com.example.dajho.data.local.entity.SyncOperationEntity

/**
 * Interfaz de Repositorio para Cola de Operaciones Pendientes de Sincronización (Fase 4).
 */
interface SyncRepository {
    suspend fun enqueueOperation(entityName: String, entityId: String, operation: String, payloadJson: String = "")
    suspend fun getPendingOperations(): List<SyncOperationEntity>
    suspend fun removeOperation(operationId: String)
}
