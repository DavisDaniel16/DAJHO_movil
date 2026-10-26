package com.example.dajho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dajho.data.local.entity.UserProfileEntity

/**
 * DAO Room para Perfil de Usuario y Autenticación (Fase 5).
 */
@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    suspend fun getUserProfile(userId: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)
}
