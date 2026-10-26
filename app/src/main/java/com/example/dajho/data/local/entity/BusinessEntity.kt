package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Comercios/Negocios (Fase 3).
 * Identificadores UUID, soporte multi-negocio y régimen fiscal RIMPE.
 */
@Entity(tableName = "businesses")
data class BusinessEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val rucOrCedula: String,
    val phone: String,
    val address: String = "",
    val rimpeType: String = "RIMPE_POPULAR", // RIMPE_POPULAR, RIMPE_EMPRENDEDOR, GENERAL
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
