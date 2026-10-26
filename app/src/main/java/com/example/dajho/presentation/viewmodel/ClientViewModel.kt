package com.example.dajho.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dajho.data.local.entity.ClientEntity
import com.example.dajho.data.local.entity.SupplierEntity
import com.example.dajho.domain.repository.ClientRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de Clientes, Proveedores y Deudas (Fase 8).
 */
class ClientViewModel(
    private val repository: ClientRepository
) : ViewModel() {

    val clientsState: StateFlow<List<ClientEntity>> = repository.getClientsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val suppliersState: StateFlow<List<SupplierEntity>> = repository.getSuppliersFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveClient(client: ClientEntity) {
        viewModelScope.launch {
            repository.saveClient(client)
        }
    }

    fun saveSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            repository.saveSupplier(supplier)
        }
    }
}
