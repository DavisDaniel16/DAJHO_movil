package com.example.dajho.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

/**
 * Gestor DataStore Preferences para la sesión activa local y autenticación (Fase 5).
 */
class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_BUSINESS_ID = stringPreferencesKey("business_id")
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("supabase_access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("supabase_refresh_token")
        private val KEY_PENDING_BUSINESS_NAME = stringPreferencesKey("pending_business_name")
        private val KEY_PENDING_BUSINESS_RUC = stringPreferencesKey("pending_business_ruc")
        private val KEY_PENDING_BUSINESS_PHONE = stringPreferencesKey("pending_business_phone")
        private val KEY_PENDING_BUSINESS_ADDRESS = stringPreferencesKey("pending_business_address")
        private val KEY_PENDING_BUSINESS_RIMPE = stringPreferencesKey("pending_business_rimpe")
    }

    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    val userIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ID]
    }

    val userNameFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME]
    }

    val userEmailFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL]
    }

    val businessIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_BUSINESS_ID]
    }

    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { prefs -> prefs[KEY_ACCESS_TOKEN] }
    val refreshTokenFlow: Flow<String?> = context.dataStore.data.map { prefs -> prefs[KEY_REFRESH_TOKEN] }

    val pendingBusinessFlow: Flow<PendingBusiness?> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_PENDING_BUSINESS_NAME] ?: return@map null
        PendingBusiness(
            name,
            prefs[KEY_PENDING_BUSINESS_RUC].orEmpty(),
            prefs[KEY_PENDING_BUSINESS_PHONE].orEmpty(),
            prefs[KEY_PENDING_BUSINESS_ADDRESS].orEmpty(),
            prefs[KEY_PENDING_BUSINESS_RIMPE] ?: "RIMPE_POPULAR"
        )
    }

    suspend fun savePendingBusiness(business: PendingBusiness) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PENDING_BUSINESS_NAME] = business.name
            prefs[KEY_PENDING_BUSINESS_RUC] = business.rucOrCedula
            prefs[KEY_PENDING_BUSINESS_PHONE] = business.phone
            prefs[KEY_PENDING_BUSINESS_ADDRESS] = business.address
            prefs[KEY_PENDING_BUSINESS_RIMPE] = business.rimpeType
        }
    }

    suspend fun clearPendingBusiness() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_PENDING_BUSINESS_NAME)
            prefs.remove(KEY_PENDING_BUSINESS_RUC)
            prefs.remove(KEY_PENDING_BUSINESS_PHONE)
            prefs.remove(KEY_PENDING_BUSINESS_ADDRESS)
            prefs.remove(KEY_PENDING_BUSINESS_RIMPE)
        }
    }

    suspend fun saveSession(
        userId: String,
        userName: String,
        email: String,
        businessId: String,
        accessToken: String? = null,
        refreshToken: String? = null
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_NAME] = userName
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_BUSINESS_ID] = businessId
            accessToken?.let { prefs[KEY_ACCESS_TOKEN] = it }
            refreshToken?.let { prefs[KEY_REFRESH_TOKEN] = it }
        }

    }

    suspend fun saveAuthenticatedUser(
        userId: String,
        email: String,
        accessToken: String,
        refreshToken: String?
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_ACCESS_TOKEN] = accessToken
            refreshToken?.let { prefs[KEY_REFRESH_TOKEN] = it }
            prefs[KEY_IS_LOGGED_IN] = false
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}

data class PendingBusiness(
    val name: String,
    val rucOrCedula: String,
    val phone: String,
    val address: String,
    val rimpeType: String
)
