package com.example.dajho.domain.repository

import com.example.dajho.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de Repositorio para Autenticación y Perfil (Fase 5).
 */
interface AuthRepository {
    fun isLoggedInFlow(): Flow<Boolean>
    fun activeUserNameFlow(): Flow<String>
    suspend fun getUserByEmail(email: String): UserProfileEntity?
    suspend fun registerLocalUser(
        fullName: String,
        email: String,
        phone: String,
        businessName: String,
        passwordHash: String,
        rucOrCedula: String = "",
        businessPhone: String = phone,
        address: String = "",
        rimpeType: String = "RIMPE_POPULAR"
    ): Result<UserProfileEntity>

    suspend fun loginLocalUser(email: String, passwordHash: String): Result<UserProfileEntity>
    suspend fun logout()
}
