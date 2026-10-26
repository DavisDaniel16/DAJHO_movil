package com.example.dajho.data.repository

import com.example.dajho.data.local.dao.ClientSupplierDao
import com.example.dajho.data.local.dao.SyncOperationDao
import com.example.dajho.data.local.entity.ClientEntity
import com.example.dajho.data.local.entity.DebtPaymentEntity
import com.example.dajho.data.local.entity.SupplierEntity
import com.example.dajho.data.local.entity.SyncOperationEntity
import com.example.dajho.domain.repository.ClientRepository
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

/**
 * Implementación del Repositorio de Clientes y Proveedores con Room y Transacción de Abonos (Fase 8).
 */
class ClientRepositoryImpl(
    private val clientDao: ClientSupplierDao,
    private val syncDao: SyncOperationDao
) : ClientRepository {

    override fun getClientsFlow(businessId: String): Flow<List<ClientEntity>> =
        clientDao.getAllClientsFlow(businessId)

    override fun getSuppliersFlow(businessId: String): Flow<List<SupplierEntity>> =
        clientDao.getAllSuppliersFlow(businessId)

    override fun getClientDebtPaymentsFlow(clientId: String): Flow<List<DebtPaymentEntity>> =
        clientDao.getClientDebtPaymentsFlow(clientId)

    override suspend fun saveClient(client: ClientEntity) {
        clientDao.insertClient(client)
        syncDao.insertOperation(
            SyncOperationEntity(
                entityName = "clients",
                entityId = client.id,
                operation = "INSERT",
                payloadJson = JSONObject().apply {
                    put("id", client.id)
                    put("negocio_id", client.businessId)
                    put("nombre", client.name)
                    put("telefono", client.phone)
                    put("info", client.info)
                    put("deuda_centavos", client.debtCents)
                }.toString()
            )
        )
    }

    override suspend fun saveSupplier(supplier: SupplierEntity) {
        clientDao.insertSupplier(supplier)
        syncDao.insertOperation(
            SyncOperationEntity(
                entityName = "suppliers",
                entityId = supplier.id,
                operation = "INSERT",
                payloadJson = JSONObject().apply {
                    put("id", supplier.id)
                    put("negocio_id", supplier.businessId)
                    put("nombre", supplier.name)
                    put("telefono", supplier.phone)
                    put("info", supplier.info)
                    put("deuda_centavos", supplier.debtCents)
                }.toString()
            )
        )
    }

    override suspend fun recordClientDebtPayment(payment: DebtPaymentEntity, activeShiftId: String?) {
        clientDao.recordClientDebtPaymentTransaction(payment, activeShiftId)
    }

    override suspend fun recordSupplierPayment(payment: DebtPaymentEntity) {
        clientDao.recordSupplierPaymentTransaction(payment)
    }
}
