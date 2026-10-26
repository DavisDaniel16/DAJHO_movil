package com.example.dajho.data.repository

import com.example.dajho.data.local.dao.BusinessDao
import com.example.dajho.data.local.dao.SyncOperationDao
import com.example.dajho.data.local.dao.UserProfileDao
import com.example.dajho.data.local.datastore.UserPreferences
import com.example.dajho.data.local.entity.BusinessEntity
import com.example.dajho.data.local.entity.SyncOperationEntity
import com.example.dajho.data.local.entity.UserProfileEntity
import com.example.dajho.domain.repository.AuthRepository
import com.example.dajho.util.PasswordUtils
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Implementación del Repositorio de Autenticación con Room, DataStore y Supabase Auth Placeholder (Fase 5).
 */
class AuthRepositoryImpl(
    private val userProfileDao: UserProfileDao,
    private val businessDao: BusinessDao,
    private val syncDao: SyncOperationDao,
    private val userPreferences: UserPreferences
) : AuthRepository {

    override fun isLoggedInFlow(): Flow<Boolean> = userPreferences.isLoggedInFlow

    override fun activeUserNameFlow(): Flow<String> = userPreferences.userNameFlow

    override suspend fun getUserByEmail(email: String): UserProfileEntity? =
        userProfileDao.getUserByEmail(email)

    override suspend fun registerLocalUser(
        fullName: String,
        email: String,
        phone: String,
        businessName: String,
        passwordHash: String
    ): Result<UserProfileEntity> {
        val existing = userProfileDao.getUserByEmail(email)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("El correo electrónico ya está registrado"))
        }

        val businessId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()

        val business = BusinessEntity(
            id = businessId,
            name = businessName,
            rucOrCedula = "",
            phone = phone
        )

        val profile = UserProfileEntity(
            userId = userId,
            businessId = businessId,
            fullName = fullName,
            email = email,
            phone = phone,
            role = "PROPIETARIO"
        )

        businessDao.insertBusiness(business)
        userProfileDao.insertUserProfile(profile)

        syncDao.insertOperation(
            SyncOperationEntity(
                entityName = "user_profiles",
                entityId = userId,
                operation = "INSERT",
                payloadJson = "{\"email\":\"$email\",\"fullName\":\"$fullName\"}"
            )
        )

        userPreferences.saveSession(
            userId = userId,
            userName = fullName,
            email = email,
            businessId = businessId
        )

        return Result.success(profile)
    }

    override suspend fun loginLocalUser(email: String, passwordHash: String): Result<UserProfileEntity> {
        val user = userProfileDao.getUserByEmail(email)
            ?: return Result.failure(IllegalArgumentException("El usuario o correo no existe"))

        userPreferences.saveSession(
            userId = user.userId,
            userName = user.fullName,
            email = user.email,
            businessId = user.businessId
        )

        return Result.success(user)
    }

    override suspend fun logout() {
        userPreferences.clearSession()
    }
}
