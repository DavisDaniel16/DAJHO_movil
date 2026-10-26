package com.example.dajho.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dajho.data.local.entity.PaymentEntity
import com.example.dajho.data.local.entity.SaleEntity
import com.example.dajho.data.local.entity.SaleItemEntity
import com.example.dajho.domain.repository.SaleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para el Punto de Venta (POS) y Checkout Atómico (Fase 7).
 */
class PosViewModel(
    private val saleRepository: SaleRepository
) : ViewModel() {

    val salesState: StateFlow<List<SaleEntity>> = saleRepository.getAllSalesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun completeSale(sale: SaleEntity, items: List<SaleItemEntity>, payments: List<PaymentEntity>, activeShiftId: String? = null) {
        viewModelScope.launch {
            saleRepository.recordCompleteSaleTransaction(sale, items, payments, activeShiftId)
        }
    }
}
