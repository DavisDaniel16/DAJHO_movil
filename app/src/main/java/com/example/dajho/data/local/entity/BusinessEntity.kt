package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Negocios/Comercios (Tabla "negocios").
 */
@Entity(tableName = "negocios")
data class BusinessEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val rucOrCedula: String,
    val phone: String,
    val address: String = "",
    val rimpeType: String = "RIMPE_POPULAR",
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
