package com.example.dajho.domain.repository

import com.example.dajho.data.local.entity.ClientEntity
import com.example.dajho.data.local.entity.DebtPaymentEntity
import com.example.dajho.data.local.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de Repositorio para Clientes, Proveedores y Abonos de Deudas (Fase 8).
 */
interface ClientRepository {
    fun getClientsFlow(businessId: String = "DEFAULT_BUSINESS"): Flow<List<ClientEntity>>
    fun getSuppliersFlow(businessId: String = "DEFAULT_BUSINESS"): Flow<List<SupplierEntity>>
    fun getClientDebtPaymentsFlow(clientId: String): Flow<List<DebtPaymentEntity>>
    suspend fun saveClient(client: ClientEntity)
    suspend fun saveSupplier(supplier: SupplierEntity)
    suspend fun recordClientDebtPayment(payment: DebtPaymentEntity, activeShiftId: String? = null)
    suspend fun recordSupplierPayment(payment: DebtPaymentEntity)
}
