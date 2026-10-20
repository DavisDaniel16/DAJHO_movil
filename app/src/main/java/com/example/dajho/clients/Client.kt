package com.example.dajho.clients

/**
 * Modelo de UI para la gestion de clientes (Fase 2).
 * Solo frontend: los datos se almacenan en memoria.
 */
data class Client(
    val id: Long,
    var name: String,
    var phone: String,
    var info: String = "",
    var debt: Double = 0.0
) {
    fun hasDebt(): Boolean = debt > 0.001
}
