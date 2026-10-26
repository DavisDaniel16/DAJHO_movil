package com.example.dajho.data.repository

import com.example.dajho.data.local.dao.SaleDao
import com.example.dajho.data.local.entity.PaymentEntity
import com.example.dajho.data.local.entity.SaleEntity
import com.example.dajho.data.local.entity.SaleItemEntity
import com.example.dajho.data.local.relation.SaleWithItemsAndPayments
import com.example.dajho.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación del Repositorio de Ventas respaldado por Room en Transacción Atómica (Fase 7).
 */
class SaleRepositoryImpl(
    private val saleDao: SaleDao
) : SaleRepository {

    override fun getAllSalesFlow(): Flow<List<SaleEntity>> = saleDao.getAllSalesFlow()

    override fun getAllSalesWithDetailsFlow(): Flow<List<SaleWithItemsAndPayments>> =
        saleDao.getAllSalesWithDetailsFlow()

    override suspend fun getSaleById(saleId: String): SaleEntity? =
        saleDao.getSaleById(saleId)

    override suspend fun recordCompleteSaleTransaction(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        payments: List<PaymentEntity>,
        activeShiftId: String?
    ) {
        saleDao.recordCompleteSale(sale, items, payments, activeShiftId)
    }
}
