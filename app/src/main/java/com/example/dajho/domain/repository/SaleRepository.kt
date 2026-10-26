package com.example.dajho.domain.repository

import com.example.dajho.data.local.entity.PaymentEntity
import com.example.dajho.data.local.entity.SaleEntity
import com.example.dajho.data.local.entity.SaleItemEntity
import com.example.dajho.data.local.relation.SaleWithItemsAndPayments
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de Repositorio para Ventas y Checkout Atómico (Fase 7).
 */
interface SaleRepository {
    fun getAllSalesFlow(): Flow<List<SaleEntity>>
    fun getAllSalesWithDetailsFlow(): Flow<List<SaleWithItemsAndPayments>>
    suspend fun getSaleById(saleId: String): SaleEntity?
    suspend fun recordCompleteSaleTransaction(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        payments: List<PaymentEntity>,
        activeShiftId: String? = null
    )
}
