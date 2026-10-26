package com.example.dajho.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad Room para Perfiles de Usuario / Empleados (Fase 3).
 */
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val userId: String = UUID.randomUUID().toString(),
    val businessId: String,
    val fullName: String,
    val email: String,
    val phone: String = "",
    val role: String = "PROPIETARIO", // PROPIETARIO, CAJERO, VENDEDOR
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
